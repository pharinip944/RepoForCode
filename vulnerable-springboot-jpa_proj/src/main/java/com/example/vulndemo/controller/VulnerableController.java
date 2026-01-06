
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


}
