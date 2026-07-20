package com.bulletin.service;

import com.bulletin.dto.bulletin.ReportCardActionRequest;
import com.bulletin.dto.bulletin.ReportCardResponse;
import com.bulletin.dto.bulletin.ReportCardWorkflowResponse;
import com.bulletin.entity.ReportCard;
import com.bulletin.exception.ResourceNotFoundException;
import com.bulletin.repository.ReportCardRepository;
import com.bulletin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportCardWorkflowService {

    private final ReportCardRepository reportCardRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public ReportCardWorkflowResponse validateByPrefet(Long reportCardId) {
        assertPrefetOrDirection();
        ReportCard reportCard = findById(reportCardId);
        if (!ReportCard.Statut.BROUILLON.name().equals(reportCard.getStatut())) {
            throw new IllegalStateException("Seuls les bulletins en brouillon peuvent être validés par le préfet");
        }
        reportCard.setStatut(ReportCard.Statut.VALIDE_PREFET.name());
        reportCard.setValideParPrefetAt(java.time.LocalDateTime.now());
        reportCardRepository.save(reportCard);
        log.info("Bulletin {} validé par le préfet", reportCardId);
        return toWorkflowResponse(reportCard);
    }

    @Transactional
    public ReportCardWorkflowResponse validateByDirecteur(Long reportCardId, ReportCardActionRequest request) {
        assertDirecteur();
        ReportCard reportCard = findById(reportCardId);
        if (!ReportCard.Statut.VALIDE_PREFET.name().equals(reportCard.getStatut())) {
            throw new IllegalStateException("Seuls les bulletins validés par le préfet peuvent être validés par le directeur");
        }
        reportCard.setStatut(ReportCard.Statut.VALIDE_DIRECTEUR.name());
        reportCard.setValideParDirecteurAt(java.time.LocalDateTime.now());
        if (request != null && request.getSignatureUrl() != null) {
            reportCard.setSignatureUrl(request.getSignatureUrl());
        }
        reportCardRepository.save(reportCard);
        log.info("Bulletin {} validé par le directeur", reportCardId);
        return toWorkflowResponse(reportCard);
    }

    @Transactional
    public ReportCardWorkflowResponse sign(Long reportCardId, ReportCardActionRequest request) {
        assertDirecteur();
        ReportCard reportCard = findById(reportCardId);
        if (!ReportCard.Statut.VALIDE_DIRECTEUR.name().equals(reportCard.getStatut())) {
            throw new IllegalStateException("Seuls les bulletins validés par le directeur peuvent être signés");
        }
        reportCard.setStatut(ReportCard.Statut.SIGNE.name());
        reportCard.setSigneAt(java.time.LocalDateTime.now());
        if (request != null && request.getSignatureUrl() != null) {
            reportCard.setSignatureUrl(request.getSignatureUrl());
        }
        reportCardRepository.save(reportCard);
        log.info("Bulletin {} signé", reportCardId);
        return toWorkflowResponse(reportCard);
    }

    @Transactional
    public ReportCardWorkflowResponse publish(Long reportCardId) {
        assertDirection();
        ReportCard reportCard = findById(reportCardId);
        if (!ReportCard.Statut.SIGNE.name().equals(reportCard.getStatut())) {
            throw new IllegalStateException("Seuls les bulletins signés peuvent être publiés");
        }
        reportCard.setStatut(ReportCard.Statut.PUBLIE.name());
        reportCard.setPublieAt(java.time.LocalDateTime.now());
        reportCardRepository.save(reportCard);
        log.info("Bulletin {} publié", reportCardId);
        return toWorkflowResponse(reportCard);
    }

    private void assertPrefetOrDirection() {
        if (securityUtils.isDirection()) {
            return;
        }
        if (securityUtils.hasRole("PREFET")) {
            return;
        }
        throw new SecurityException("Accès refusé : réservé au préfet ou à la direction");
    }

    private void assertDirecteur() {
        if (securityUtils.hasRole("DIRECTEUR") || securityUtils.isAdmin()) {
            return;
        }
        throw new SecurityException("Accès refusé : réservé au directeur");
    }

    private void assertDirection() {
        if (!securityUtils.isDirection()) {
            throw new SecurityException("Accès refusé : réservé à la direction");
        }
    }

    private ReportCard findById(Long id) {
        return reportCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bulletin non trouvé avec l'ID: " + id));
    }

    private ReportCardWorkflowResponse toWorkflowResponse(ReportCard reportCard) {
        return ReportCardWorkflowResponse.builder()
                .id(reportCard.getId())
                .statut(ReportCard.Statut.valueOf(reportCard.getStatut()))
                .signatureUrl(reportCard.getSignatureUrl())
                .valideParPrefetAt(reportCard.getValideParPrefetAt())
                .valideParDirecteurAt(reportCard.getValideParDirecteurAt())
                .signeAt(reportCard.getSigneAt())
                .publieAt(reportCard.getPublieAt())
                .build();
    }
}
