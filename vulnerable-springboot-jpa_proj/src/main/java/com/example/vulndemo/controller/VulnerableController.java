
package com.example.vulndemo.controller;

import com.example.vulndemo.repo.RawJpaLoginRepo;
import com.example.vulndemo.util.InsecureDeserializer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Random;

/** Controller with intentionally vulnerable endpoints. */
@RestController
public class VulnerableController {

    private final RawJpaLoginRepo repo;
    private final RestTemplate restTemplate = new RestTemplate(); // No timeouts; SSRF risk

    // Hard-coded secret (also in application.properties)
    @Value("${app.jwt.secret:hardcoded-super-secret}")
    private String jwtSecret;

    public VulnerableController(RawJpaLoginRepo repo) {
        this.repo = repo;
    }

    /** SQL Injection + PII logging */
    @GetMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        System.out.println("Attempting login with username=" + username + " password=" + password); // PII logging
        boolean ok = repo.login(username, password);
        return ResponseEntity.ok(ok ? "Login OK" : "Login Failed");
    }


    /** Reflected XSS: returns HTML with unescaped term */
    @GetMapping(value = "/search", produces = MediaType.TEXT_HTML_VALUE)
    public String search(@RequestParam String term) {
        return "<html><body>Results for: " + term + "<br/>" +
                "<script>console.log('Inline script allowed');</script>" +
                "</body></html>";
    }



    /** Command injection: executes arbitrary command */
    @GetMapping("/exec")
    public ResponseEntity<String> exec(@RequestParam String cmd) throws IOException {
        Process p = Runtime.getRuntime().exec(cmd);
        String output = new String(StreamUtils.copyToByteArray(p.getInputStream()), StandardCharsets.UTF_8);
        return ResponseEntity.ok(output);
    }

    /** Path traversal on upload: writes to user-provided path */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam("path") String path) throws Exception {
        File out = new File(path);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(file.getBytes());
        }
        return ResponseEntity.ok("Saved to " + out.getAbsolutePath());
    }

    /** Read arbitrary file path */
    @GetMapping("/file")
    public ResponseEntity<String> readFile(@RequestParam String name) throws Exception {
        File f = new File(name);
        try (FileInputStream fis = new FileInputStream(f)) {
            String content = new String(StreamUtils.copyToByteArray(fis), StandardCharsets.UTF_8);
            return ResponseEntity.ok(content);
        }
    }

    /** SSRF: fetches arbitrary URL without validation */
    @GetMapping("/fetch")
    public ResponseEntity<String> fetch(@RequestParam String url) {
        String body = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(body);
    }

    /** Open redirect */
    @GetMapping("/redirect")
    public RedirectView redirect(@RequestParam String to) {
        return new RedirectView(to);
    }

    /** Insecure deserialization */
    @PostMapping(value = "/deserialize", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<String> deserialize(@RequestBody byte[] bytes) throws Exception {
        Object obj = InsecureDeserializer.deserialize(bytes);
        return ResponseEntity.ok("Deserialized: " + obj);
    }

    /** Predictable random vs SecureRandom example */
    @GetMapping("/random-token")
    public ResponseEntity<String> randomToken(@RequestParam(defaultValue = "weak") String type) {
        byte[] buf = new byte[16];
        if ("weak".equalsIgnoreCase(type)) {
            new Random().nextBytes(buf); // predictable
        } else {
            new SecureRandom().nextBytes(buf);
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : buf) sb.append(String.format("%02x", b));
        return ResponseEntity.ok(sb.toString());
    }

    /** Hardcoded secret exposure */
    @GetMapping("/secrets")
    public ResponseEntity<String> secrets() {
        return ResponseEntity.ok("JWT_SECRET=" + jwtSecret);
    }
}
