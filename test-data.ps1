# Script de peuplement des données de test pour le bulletin RDC
# Usage: .\test-data.ps1

$baseUrl = "http://localhost:8000"

# 1. Login super admin initial
$login = Invoke-RestMethod -Uri "$baseUrl/auth/token" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$token = $login.accessToken
Write-Host "Token super admin: $token"
$headers = @{ Authorization = "Bearer $token" }

# 2. Creer ecole (l'admin est cree automatiquement avec email comme username et mot de passe 123456)
$schoolBody = @{nom="Ecole Primaire Muviki";code="EPM";province="Kinshasa";communeTerritoire="Lemba";adresse="123 Avenue de l'Independance";telephone="+243 81 234 5678";email="contact@muviki.cd"} | ConvertTo-Json
$school = Invoke-RestMethod -Uri "$baseUrl/api/ecoles" -Method POST -Headers $headers -ContentType "application/json" -Body $schoolBody
Write-Host "Ecole creee: $($school.id) - $($school.nom)"

# Login avec l'admin automatique de l'ecole (email comme username, mot de passe 123456)
$adminUsername = $school.email
$adminPassword = "123456"
$login = Invoke-RestMethod -Uri "$baseUrl/auth/token" -Method POST -ContentType "application/json" -Body (@{username=$adminUsername;password=$adminPassword} | ConvertTo-Json)
$token = $login.accessToken
Write-Host "Login admin auto: $adminUsername / $adminPassword"
$headers = @{ Authorization = "Bearer $token" }

# 3. Creer annee academique
$yearBody = @{schoolId=$school.id;libelle="2025-2026";dateDebut="2025-09-01";dateFin="2026-09-15"} | ConvertTo-Json
$year = Invoke-RestMethod -Uri "$baseUrl/api/annees-academiques" -Method POST -Headers $headers -ContentType "application/json" -Body $yearBody
Write-Host "Annee creee: $($year.id) - $($year.libelle)"

# 4. Creer niveau
$level = Invoke-RestMethod -Uri "$baseUrl/api/niveaux" -Method POST -Headers $headers -ContentType "application/json" -Body '{"nom":"Primaire","ordre":1}'
Write-Host "Niveau cree: $($level.id) - $($level.nom)"

# 5. Creer section
$section = Invoke-RestMethod -Uri "$baseUrl/api/sections" -Method POST -Headers $headers -ContentType "application/json" -Body '{"nom":"Francais"}'
Write-Host "Section creee: $($section.id) - $($section.nom)"

# 6. Creer option
$optionBody = @{nom="Ordinaire";sectionId=$section.id} | ConvertTo-Json
$option = Invoke-RestMethod -Uri "$baseUrl/api/options" -Method POST -Headers $headers -ContentType "application/json" -Body $optionBody
Write-Host "Option creee: $($option.id) - $($option.nom)"

# 7. Creer classe
$classroomBody = @{nom="3eme A";academicYearId=$year.id;levelId=$level.id;sectionId=$section.id;optionId=$option.id} | ConvertTo-Json
$classroom = Invoke-RestMethod -Uri "$baseUrl/api/salles" -Method POST -Headers $headers -ContentType "application/json" -Body $classroomBody
Write-Host "Classe creee: $($classroom.id) - $($classroom.nom)"

# 8. Creer matieres
$math = Invoke-RestMethod -Uri "$baseUrl/api/matieres" -Method POST -Headers $headers -ContentType "application/json" -Body '{"nom":"Mathematiques","code":"MAT"}'
$french = Invoke-RestMethod -Uri "$baseUrl/api/matieres" -Method POST -Headers $headers -ContentType "application/json" -Body '{"nom":"Francais","code":"FRA"}'
$science = Invoke-RestMethod -Uri "$baseUrl/api/matieres" -Method POST -Headers $headers -ContentType "application/json" -Body '{"nom":"Sciences","code":"SCI"}'
$history = Invoke-RestMethod -Uri "$baseUrl/api/matieres" -Method POST -Headers $headers -ContentType "application/json" -Body '{"nom":"Histoire","code":"HIS"}'
Write-Host "Matieres creees: Math, Francais, Sciences, Histoire"

# 9. Creer enseignant
$teacherBody = @{nom="Mbuyamba";postnom="Jean";prenom="Pierre";specialite="Mathematiques";telephone="+243 82 111 2222";email="jean.mbuyamba@ecole.cd"} | ConvertTo-Json
$teacher = Invoke-RestMethod -Uri "$baseUrl/api/enseignants" -Method POST -Headers $headers -ContentType "application/json" -Body $teacherBody
Write-Host "Enseignant cree: $($teacher.id) - $($teacher.nom) $($teacher.postnom)"

# 10. Creer affectation
$assignmentBody = @{teacherId=$teacher.id;classroomId=$classroom.id;subjectId=$math.id} | ConvertTo-Json
$assignment = Invoke-RestMethod -Uri "$baseUrl/api/attributions-enseignement" -Method POST -Headers $headers -ContentType "application/json" -Body $assignmentBody
Write-Host "Affectation creee: $($assignment.id)"

# 11. Creer types d'evaluation
$interro = Invoke-RestMethod -Uri "$baseUrl/api/types-evaluations" -Method POST -Headers $headers -ContentType "application/json" -Body '{"nom":"Interrogation","coefficient":1}'
$devoir = Invoke-RestMethod -Uri "$baseUrl/api/types-evaluations" -Method POST -Headers $headers -ContentType "application/json" -Body '{"nom":"Devoir","coefficient":2}'
$examen = Invoke-RestMethod -Uri "$baseUrl/api/types-evaluations" -Method POST -Headers $headers -ContentType "application/json" -Body '{"nom":"Examen","coefficient":4}'
Write-Host "Types d'evaluation crees: Interrogation, Devoir, Examen"

# 12. Creer 3 trimestres
$trimestres = @()
$trimestres += Invoke-RestMethod -Uri "$baseUrl/api/trimestres" -Method POST -Headers $headers -ContentType "application/json" -Body (@{academicYearId=$year.id;nom="1er Trimestre";ordre=1;dateDebut="2025-09-01";dateFin="2025-12-15"} | ConvertTo-Json)
$trimestres += Invoke-RestMethod -Uri "$baseUrl/api/trimestres" -Method POST -Headers $headers -ContentType "application/json" -Body (@{academicYearId=$year.id;nom="2e Trimestre";ordre=2;dateDebut="2026-01-05";dateFin="2026-04-15"} | ConvertTo-Json)
$trimestres += Invoke-RestMethod -Uri "$baseUrl/api/trimestres" -Method POST -Headers $headers -ContentType "application/json" -Body (@{academicYearId=$year.id;nom="3e Trimestre";ordre=3;dateDebut="2026-04-16";dateFin="2026-09-15"} | ConvertTo-Json)
Write-Host "Trimestres crees: $($trimestres.Count)"

# 13. Creer periodes sous chaque trimestre (2 periodes + examen par trimestre)
$periodes = @()
$periodes += Invoke-RestMethod -Uri "$baseUrl/api/periodes" -Method POST -Headers $headers -ContentType "application/json" -Body (@{trimesterId=$trimestres[0].id;nom="Periode 1";ordre=1;type="PERIODE";dateDebut="2025-09-01";dateFin="2025-10-15"} | ConvertTo-Json)
$periodes += Invoke-RestMethod -Uri "$baseUrl/api/periodes" -Method POST -Headers $headers -ContentType "application/json" -Body (@{trimesterId=$trimestres[0].id;nom="Periode 2";ordre=2;type="PERIODE";dateDebut="2025-10-16";dateFin="2025-12-15"} | ConvertTo-Json)
$periodes += Invoke-RestMethod -Uri "$baseUrl/api/periodes" -Method POST -Headers $headers -ContentType "application/json" -Body (@{trimesterId=$trimestres[0].id;nom="Examen 1";ordre=3;type="EXAMEN";dateDebut="2026-01-05";dateFin="2026-01-30"} | ConvertTo-Json)
$periodes += Invoke-RestMethod -Uri "$baseUrl/api/periodes" -Method POST -Headers $headers -ContentType "application/json" -Body (@{trimesterId=$trimestres[1].id;nom="Periode 3";ordre=1;type="PERIODE";dateDebut="2026-02-01";dateFin="2026-03-15"} | ConvertTo-Json)
$periodes += Invoke-RestMethod -Uri "$baseUrl/api/periodes" -Method POST -Headers $headers -ContentType "application/json" -Body (@{trimesterId=$trimestres[1].id;nom="Periode 4";ordre=2;type="PERIODE";dateDebut="2026-03-16";dateFin="2026-05-15"} | ConvertTo-Json)
$periodes += Invoke-RestMethod -Uri "$baseUrl/api/periodes" -Method POST -Headers $headers -ContentType "application/json" -Body (@{trimesterId=$trimestres[1].id;nom="Examen 2";ordre=3;type="EXAMEN";dateDebut="2026-05-16";dateFin="2026-06-15"} | ConvertTo-Json)
$periodes += Invoke-RestMethod -Uri "$baseUrl/api/periodes" -Method POST -Headers $headers -ContentType "application/json" -Body (@{trimesterId=$trimestres[2].id;nom="Periode 5";ordre=1;type="PERIODE";dateDebut="2026-06-16";dateFin="2026-07-15"} | ConvertTo-Json)
$periodes += Invoke-RestMethod -Uri "$baseUrl/api/periodes" -Method POST -Headers $headers -ContentType "application/json" -Body (@{trimesterId=$trimestres[2].id;nom="Periode 6";ordre=2;type="PERIODE";dateDebut="2026-07-16";dateFin="2026-08-15"} | ConvertTo-Json)
$periodes += Invoke-RestMethod -Uri "$baseUrl/api/periodes" -Method POST -Headers $headers -ContentType "application/json" -Body (@{trimesterId=$trimestres[2].id;nom="Examen 3";ordre=3;type="EXAMEN";dateDebut="2026-08-16";dateFin="2026-09-15"} | ConvertTo-Json)
Write-Host "Periodes creees: $($periodes.Count) (3 trimestres x 2 periodes + examen)"

# 14. Creer eleves
$students = @()
$students += Invoke-RestMethod -Uri "$baseUrl/api/eleves" -Method POST -Headers $headers -ContentType "application/json" -Body (@{matricule="EL-2026-001";nom="Kalala";postnom="Jean";prenom="";sexe="M";dateNaissance="2015-03-10";lieuNaissance="Kinshasa";adresse="123 Avenue de la Paix";telephoneParent="+243 83 111 1111";emailParent="parent.kalala@example.com";etat="actif"} | ConvertTo-Json)
$students += Invoke-RestMethod -Uri "$baseUrl/api/eleves" -Method POST -Headers $headers -ContentType "application/json" -Body (@{matricule="EL-2026-002";nom="Mukendi";postnom="Marie";prenom="";sexe="F";dateNaissance="2014-07-22";lieuNaissance="Lubumbashi";adresse="456 Boulevard du 30 Juin";telephoneParent="+243 84 222 2222";emailParent="parent.mukendi@example.com";etat="actif"} | ConvertTo-Json)
$students += Invoke-RestMethod -Uri "$baseUrl/api/eleves" -Method POST -Headers $headers -ContentType "application/json" -Body (@{matricule="EL-2026-003";nom="Tshibola";postnom="Pierre";prenom="";sexe="M";dateNaissance="2015-11-05";lieuNaissance="Goma";adresse="789 Avenue des Volcans";telephoneParent="+243 85 333 3333";emailParent="parent.tshibola@example.com";etat="actif"} | ConvertTo-Json)
$students += Invoke-RestMethod -Uri "$baseUrl/api/eleves" -Method POST -Headers $headers -ContentType "application/json" -Body (@{matricule="EL-2026-004";nom="Kapinga";postnom="Sarah";prenom="";sexe="F";dateNaissance="2014-12-15";lieuNaissance="Kinshasa";adresse="321 Rue des Rosiers";telephoneParent="+243 86 444 4444";emailParent="parent.kapinga@example.com";etat="actif"} | ConvertTo-Json)
$students += Invoke-RestMethod -Uri "$baseUrl/api/eleves" -Method POST -Headers $headers -ContentType "application/json" -Body (@{matricule="EL-2026-005";nom="Mutombo";postnom="David";prenom="";sexe="M";dateNaissance="2015-01-30";lieuNaissance="Kolwezi";adresse="654 Avenue des Mines";telephoneParent="+243 87 555 5555";emailParent="parent.mutombo@example.com";etat="actif"} | ConvertTo-Json)
Write-Host "Eleves crees: $($students.Count)"

# 15. Creer inscriptions
$enrollments = @()
foreach ($student in $students) {
    $enrollmentBody = @{studentId=$student.id;classroomId=$classroom.id;dateInscription="2025-09-01"} | ConvertTo-Json
    $enrollment = Invoke-RestMethod -Uri "$baseUrl/api/inscriptions" -Method POST -Headers $headers -ContentType "application/json" -Body $enrollmentBody
    $enrollments += $enrollment
}
Write-Host "Inscriptions creees: $($enrollments.Count)"

# 16. Creer evaluations (sur la periode 1)
$assessments = @()
$assessments += Invoke-RestMethod -Uri "$baseUrl/api/evaluations" -Method POST -Headers $headers -ContentType "application/json" -Body (@{assignmentId=$assignment.id;assessmentTypeId=$interro.id;periodId=$periodes[0].id;titre="Interrogation 1";date="2025-10-10";noteMax=20;publie=$true} | ConvertTo-Json)
$assessments += Invoke-RestMethod -Uri "$baseUrl/api/evaluations" -Method POST -Headers $headers -ContentType "application/json" -Body (@{assignmentId=$assignment.id;assessmentTypeId=$devoir.id;periodId=$periodes[0].id;titre="Devoir 1";date="2025-10-15";noteMax=20;publie=$true} | ConvertTo-Json)
$assessments += Invoke-RestMethod -Uri "$baseUrl/api/evaluations" -Method POST -Headers $headers -ContentType "application/json" -Body (@{assignmentId=$assignment.id;assessmentTypeId=$examen.id;periodId=$periodes[0].id;titre="Examen 1";date="2025-10-20";noteMax=20;publie=$true} | ConvertTo-Json)
Write-Host "Evaluations creees: $($assessments.Count)"

# 17. Creer notes
$notes = @(
    @(14, 12, 16),
    @(10, 11, 13),
    @(15, 14, 17),
    @(9, 10, 12),
    @(13, 13, 15)
)

for ($i = 0; $i -lt $students.Count; $i++) {
    $student = $students[$i]
    for ($j = 0; $j -lt $assessments.Count; $j++) {
        $assessment = $assessments[$j]
        $note = $notes[$i][$j]
        $gradeBody = @{assessmentId=$assessment.id;studentId=$student.id;note=$note;absence=$false;observation="Bien"} | ConvertTo-Json
        Invoke-RestMethod -Uri "$baseUrl/api/notes" -Method POST -Headers $headers -ContentType "application/json" -Body $gradeBody
    }
}
Write-Host "Notes creees pour $($students.Count) eleves"

# 18. Creer absences/retards (sur la periode 1)
for ($i = 0; $i -lt $students.Count; $i++) {
    $student = $students[$i]
    $attendanceBody1 = @{studentId=$student.id;periodId=$periodes[0].id;date="2025-10-01";retard=$false;absence=$true;motif="Maladie"} | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/api/presences" -Method POST -Headers $headers -ContentType "application/json" -Body $attendanceBody1
    if ($i % 2 -eq 0) {
        $attendanceBody2 = @{studentId=$student.id;periodId=$periodes[0].id;date="2025-10-10";retard=$true;absence=$false;motif="Retard"} | ConvertTo-Json
        Invoke-RestMethod -Uri "$baseUrl/api/presences" -Method POST -Headers $headers -ContentType "application/json" -Body $attendanceBody2
    }
}
Write-Host "Presences creees"

# 19. Creer discipline (sur la periode 1)
foreach ($student in $students) {
    $disciplineBody = @{studentId=$student.id;periodId=$periodes[0].id;conduite="Bonne";application="Bonne";observation="Eleve serieux"} | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/api/disciplines" -Method POST -Headers $headers -ContentType "application/json" -Body $disciplineBody
}
Write-Host "Disciplines creees"

Write-Host ""
Write-Host "Donnees de test creees avec succes!"
Write-Host "Ecole: $($school.nom)"
Write-Host "Classe: $($classroom.nom)"
Write-Host "Eleves: $($students.Count)"
Write-Host "Trimestres: $($trimestres.Count)"
Write-Host "Periodes: $($periodes.Count) (3 trimestres x 2 periodes + examen)"
Write-Host ""
Write-Host "Testez la generation de bulletin avec:"
Write-Host "POST $baseUrl/api/bulletins/generer"
Write-Host "{ classroomId: $($classroom.id), periodId: $($periodes[0].id) }"
