package be.uliege.speam.team03.MDTools.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class BackupProxyService {

  @Value("${BACKUP_SERVICE_URL}")
  private String backupUrl;

  @Value("${BACKUP_SERVICE_TOKEN}")
  private String token;

  private final RestTemplate rest = new RestTemplate();

  public String listBackupsJson() {
    HttpHeaders h = new HttpHeaders();
    h.set("X-Backup-Token", token);
    HttpEntity<Void> req = new HttpEntity<>(h);

    ResponseEntity<String> res = rest.exchange(
      backupUrl + "/internal/backups",
      HttpMethod.GET,
      req,
      String.class
    );

    return res.getBody();
  }

  public String startRestore(String type, String password) {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.set("X-Backup-Token", token);

    Map<String, String> body = Map.of("type", type, "password", password);
    HttpEntity<Map<String, String>> req = new HttpEntity<>(body, h);

    ResponseEntity<String> res = rest.exchange(
      backupUrl + "/internal/restore",
      HttpMethod.POST,
      req,
      String.class
    );

    return res.getBody();
  }

  public String jobStatus(String jobId) {
    HttpHeaders h = new HttpHeaders();
    h.set("X-Backup-Token", token);
    HttpEntity<Void> req = new HttpEntity<>(h);

    ResponseEntity<String> res = rest.exchange(
      backupUrl + "/internal/jobs/" + jobId,
      HttpMethod.GET,
      req,
      String.class
    );
    return res.getBody();
  }

  public String jobLogs(String jobId, int offset) {
    HttpHeaders h = new HttpHeaders();
    h.set("X-Backup-Token", token);
    HttpEntity<Void> req = new HttpEntity<>(h);

    ResponseEntity<String> res = rest.exchange(
      backupUrl + "/internal/jobs/" + jobId + "/logs?offset=" + offset,
      HttpMethod.GET,
      req,
      String.class
    );
    return res.getBody();
  }
}
