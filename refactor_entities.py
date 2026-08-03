# -*- coding: utf-8 -*-
"""
Refactorise les entités JPA pour hériter de BaseEntity.
- Remplace @Data par @Getter @Setter (sauf si @Builder.Default présent: on garde @Data? non, on uniformise)
- Supprime @Where (remplacé par @SQLRestriction sur BaseEntity)
- Supprime le bloc id / created_at / updated_at / deleted_at / @PrePersist / @PreUpdate redondants
- Conserve la logique métier spécifique (propagation schoolId) en la réintégrant dans onCreate()
- Fait hériter la classe de BaseEntity
"""
import os
import re
import glob

ENTITY_DIR = os.path.join("src", "main", "java", "com", "bulletin", "entity")
SKIP = {"BaseEntity.java", "School.java"}  # déjà refactorisés

def extract_schoolid_logic(content):
    """Extrait le bloc if (... schoolId ...) complet dans onCreate() pour le conserver."""
    m = re.search(r'@PrePersist\s+protected void onCreate\(\)\s*\{(.*?)\n    \}', content, re.DOTALL)
    if not m:
        return None
    body = m.group(1)
    # Capture le bloc if (...){ schoolId = ...; } entier (gère l'imbrication simple)
    ifm = re.search(r'(if \([^\n]*schoolId[^\n]*\) \{\n(?:.*\n)*?\s*\})', body)
    if not ifm:
        return None
    block = ifm.group(1).rstrip()
    return block.splitlines()

def process_file(path):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    original = content
    filename = os.path.basename(path)

    # 1. Remplacer @Data par @Getter @Setter
    content = content.replace("@Data\n", "@Getter\n@Setter\n")

    # 2. Supprimer l'annotation @Where (ligne entière)
    content = re.sub(r'\s*@org\.hibernate\.annotations\.Where\(clause = "deleted_at IS NULL"\)\n', "\n", content)

    # 3. Supprimer le champ id
    content = re.sub(
        r'\s*@Id\s*\n\s*@GeneratedValue\(strategy = GenerationType\.IDENTITY\)\s*\n\s*private Long id;\s*\n',
        "\n",
        content,
    )

    # 4. Supprimer les champs created_at / updated_at / deleted_at
    content = re.sub(r'\s*@Column\(name = "created_at"\)\s*\n\s*private LocalDateTime createdAt;\s*\n', "\n", content)
    content = re.sub(r'\s*@Column\(name = "updated_at"\)\s*\n\s*private LocalDateTime updatedAt;\s*\n', "\n", content)
    content = re.sub(r'\s*@Column\(name = "deleted_at"\)\s*\n\s*private LocalDateTime deletedAt;\s*\n', "\n", content)

    # 5. Extraire la logique schoolId AVANT de supprimer onCreate/onUpdate
    schoolid_logic = extract_schoolid_logic(content)

    # 6. Supprimer les méthodes @PrePersist onCreate et @PreUpdate onUpdate
    content = re.sub(
        r'\s*@PrePersist\s*\n\s*protected void onCreate\(\)\s*\{.*?\n    \}\s*\n',
        "\n",
        content,
        flags=re.DOTALL,
    )
    content = re.sub(
        r'\s*@PreUpdate\s*\n\s*protected void onUpdate\(\)\s*\{.*?\n    \}\s*\n',
        "\n",
        content,
        flags=re.DOTALL,
    )

    # 7. Faire hériter de BaseEntity
    content = re.sub(r'public class (\w+) \{', r'public class \1 extends BaseEntity {', content, count=1)

    # 8. Réintégrer la logique schoolId dans un onCreate() override si présente
    if schoolid_logic:
        body = "\n".join(schoolid_logic)
        # Indente correctement (8 espaces dans la méthode)
        body_lines = []
        for line in body.splitlines():
            if line.strip():
                body_lines.append("        " + line.strip())
        body = "\n".join(body_lines)
        oncreate = (
            "\n    @Override\n"
            "    protected void onCreate() {\n"
            "        super.onCreate();\n"
            f"{body}\n"
            "    }\n"
        )
        # Insère avant la dernière accolade fermante de la classe
        idx = content.rstrip().rfind("}")
        content = content[:idx].rstrip() + "\n" + oncreate + "}\n"

    # 9. Supprime l'import LocalDateTime si plus utilisé (sauf si schoolId logic ou autres champs)
    if "LocalDateTime" not in content.replace("import java.time.LocalDateTime;", ""):
        content = re.sub(r'import java\.time\.LocalDateTime;\s*\n', "", content)

    # 10. Nettoie les multiples sauts de ligne
    content = re.sub(r'\n{3,}', "\n\n", content)

    if content != original:
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        return True
    return False

def main():
    files = sorted(glob.glob(os.path.join(ENTITY_DIR, "*.java")))
    changed = []
    for path in files:
        filename = os.path.basename(path)
        if filename in SKIP:
            print(f"[SKIP] {filename}")
            continue
        try:
            if process_file(path):
                changed.append(filename)
                print(f"[OK]   {filename}")
            else:
                print(f"[--]   {filename} (aucun changement)")
        except Exception as e:
            print(f"[ERR]  {filename}: {e}")
    print(f"\n{len(changed)} fichiers modifies.")

if __name__ == "__main__":
    main()
