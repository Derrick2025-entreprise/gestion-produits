# Lance les tests, la couverture JaCoCo et l'analyse SonarQube Cloud.
# Le jeton est demande au clavier (masque) : il n'est ni affiche ni enregistre.
$secure = Read-Host "Collez votre jeton SonarQube puis Entree (il ne s'affiche pas)" -AsSecureString
$env:SONAR_TOKEN = [System.Net.NetworkCredential]::new('', $secure).Password
try {
    & .\mvnw.cmd clean verify sonar:sonar
} finally {
    Remove-Item Env:SONAR_TOKEN -ErrorAction SilentlyContinue
}
