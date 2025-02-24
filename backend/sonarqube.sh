mvn clean verify sonar:sonar \
  -Dsonar.projectKey=SPEAM_2024-2025_team3_mdtools_9cc23a0e-23cc-41c4-98c6-878b2b3cc20f \
  -Dsonar.projectName='MDTools' \
  -Dsonar.host.url=https://sonarqube-mdtools.apps.speam.montefiore.uliege.be \
  -Dsonar.token=sqp_4ee8faec42c47c5190f7ef00c718fc222c61be42 -Dspring.profiles.active=test # local usage only
