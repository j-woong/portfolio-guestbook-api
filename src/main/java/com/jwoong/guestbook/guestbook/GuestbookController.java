package com.jwoong.guestbook.guestbook;

import com.jwoong.guestbook.guestbook.dto.GuestbookCreateRequest;
import com.jwoong.guestbook.guestbook.dto.GuestbookEntryResponse;
import com.jwoong.guestbook.guestbook.dto.GuestbookPageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guestbook")
public class GuestbookController {

    private final GuestbookService guestbookService;

    public GuestbookController(GuestbookService guestbookService) {
        this.guestbookService = guestbookService;
    }

    @GetMapping
    public ResponseEntity<GuestbookPageResponse> getEntries(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(guestbookService.getEntries(page, size));
    }

    @PostMapping
    public ResponseEntity<GuestbookEntryResponse> create(@Valid @RequestBody GuestbookCreateRequest request,
                                                         HttpServletRequest httpRequest) {
        GuestbookEntryResponse response = guestbookService.create(request, clientKey(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id,
                                       @RequestHeader(name = "X-Admin-Key", required = false) String adminKey) {
        guestbookService.delete(id, adminKey);
        return ResponseEntity.noContent().build();
    }

    // 배포 환경에서는 프록시 뒤에 있으므로 X-Forwarded-For의 첫 번째 값을 방문자 IP로 본다
    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].strip();
        }
        return request.getRemoteAddr();
    }
}
