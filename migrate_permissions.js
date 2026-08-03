// Migre les contrôleurs du pattern isDirection() vers securityUtils.assertPermission(code)
// Chaque contrôleur est mappé à sa permission de gestion correspondante.
const fs = require("fs");
const path = require("path");

// Mapping fichier -> permission de gestion
const MAPPING = {
  "controller/people/StudentController.java": "ELEVE_GERER",
  "controller/people/TeacherController.java": "ENSEIGNANT_GERER",
  "controller/people/EnrollmentController.java": "INSCRIPTION_GERER",
  "controller/school/ClassroomController.java": "CLASSE_GERER",
  "controller/curriculum/SubjectController.java": "MATIERE_GERER",
  "controller/school/AcademicYearController.java": "ANNEE_GERER",
  "controller/school/TrimesterController.java": "TRIMESTRE_GERER",
  "controller/school/PeriodController.java": "PERIODE_GERER",
  "controller/school/PeriodClosureController.java": "PERIODE_VERROUILLER",
};

const SRC = path.join("src", "main", "java", "com", "bulletin");

// Pattern : if (!securityUtils.isDirection()) {\n<espaces>throw new SecurityException("...");
// On le remplace par : securityUtils.assertPermission("CODE"); et on supprime le bloc if + throw.
const PATTERN = /if \(!securityUtils\.isDirection\(\)\) \{\s*\n\s*throw new SecurityException\("[^"]*"\);\s*\n\s*\}/g;

let totalChanged = 0;
for (const [rel, permission] of Object.entries(MAPPING)) {
  const filePath = path.join(SRC, rel);
  if (!fs.existsSync(filePath)) {
    console.log(`[ABSENT] ${rel}`);
    continue;
  }
  let content = fs.readFileSync(filePath, "utf8");
  const before = (content.match(PATTERN) || []).length;
  if (before === 0) {
    console.log(`[--] ${rel} (aucune occurrence)`);
    continue;
  }
  content = content.replace(PATTERN, `securityUtils.assertPermission("${permission}");`);
  fs.writeFileSync(filePath, content, "utf8");
  totalChanged += before;
  console.log(`[OK] ${rel} : ${before} occurrence(s) -> assertPermission("${permission}")`);
}
console.log(`\nTotal : ${totalChanged} occurrences remplacees.`);
