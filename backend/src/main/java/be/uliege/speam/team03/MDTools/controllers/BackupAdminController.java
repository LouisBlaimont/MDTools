package be.uliege.speam.team03.MDTools.controllers;

import be.uliege.speam.team03.MDTools.services.BackupProxyService;
import be.uliege.speam.team03.MDTools.DTOs.RestoreRequestDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/backups")
public class BackupAdminController {

  private final BackupProxyService proxy;

  public BackupAdminController(BackupProxyService proxy) {
    this.proxy = proxy;
  }

  @GetMapping
  public ResponseEntity<String> list() {
    return ResponseEntity.ok(proxy.listBackupsJson());
  }

  @PostMapping("/restore")
  public ResponseEntity<String> restore(@RequestBody RestoreRequestDTO req) {
    // req contains type + password (password checked by backup)
    String res = proxy.startRestore(req.getType(), req.getPassword());
    return ResponseEntity.accepted().body(res); // returns {"jobId":"..."}
  }

  @GetMapping("/jobs/{jobId}")
  public ResponseEntity<String> job(@PathVariable String jobId) {
    return ResponseEntity.ok(proxy.jobStatus(jobId));
  }

  @GetMapping("/jobs/{jobId}/logs")
  public ResponseEntity<String> logs(@PathVariable String jobId, @RequestParam(defaultValue = "0") int offset) {
    return ResponseEntity.ok(proxy.jobLogs(jobId, offset));
  }
}
