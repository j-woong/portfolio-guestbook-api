package com.jwoong.guestbook.guestbook;

import com.jwoong.guestbook.global.exception.BusinessException;
import com.jwoong.guestbook.global.exception.ErrorCode;
import com.jwoong.guestbook.guestbook.dto.GuestbookCreateRequest;
import com.jwoong.guestbook.guestbook.dto.GuestbookEntryResponse;
import com.jwoong.guestbook.guestbook.dto.GuestbookPageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GuestbookService {

    private static final int MAX_PAGE_SIZE = 30;

    private final GuestbookRepository guestbookRepository;
    private final WriteRateLimiter writeRateLimiter;
    private final String adminKey;

    public GuestbookService(GuestbookRepository guestbookRepository, WriteRateLimiter writeRateLimiter,
                            @Value("${app.admin-key}") String adminKey) {
        this.guestbookRepository = guestbookRepository;
        this.writeRateLimiter = writeRateLimiter;
        this.adminKey = adminKey;
    }

    // 최신 글부터 한 페이지를 조회한다. 잘못된 page·size는 에러 대신 허용 범위로 맞춘다
    public GuestbookPageResponse getEntries(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Slice<GuestbookEntry> slice = guestbookRepository.findAllByOrderByIdDesc(PageRequest.of(safePage, safeSize));
        List<GuestbookEntryResponse> entries = slice.getContent().stream().map(GuestbookEntryResponse::from).toList();
        return new GuestbookPageResponse(entries, safePage, safeSize, slice.hasNext());
    }

    // 글을 저장한다. clientKey(IP)가 대기 시간 안에 이미 글을 썼으면 429
    @Transactional
    public GuestbookEntryResponse create(GuestbookCreateRequest request, String clientKey) {
        if (!writeRateLimiter.tryAcquire(clientKey)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS_EXCEPTION);
        }
        GuestbookEntry entry = new GuestbookEntry(request.nickname().strip(), request.message().strip(),
                Boolean.TRUE.equals(request.foundCat()));
        return GuestbookEntryResponse.from(guestbookRepository.save(entry));
    }

    // 관리자 키가 맞을 때만 글을 삭제한다
    @Transactional
    public void delete(Long id, String requestKey) {
        if (!isAdmin(requestKey)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_EXCEPTION);
        }
        GuestbookEntry entry = guestbookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTRY_NOT_FOUND_EXCEPTION));
        guestbookRepository.delete(entry);
    }

    private boolean isAdmin(String requestKey) {
        if (adminKey == null || adminKey.isBlank() || requestKey == null) {
            return false;
        }
        // 글자 수만큼 항상 같은 시간이 걸리는 비교. 응답 시간으로 키를 추측하지 못하게 한다
        return MessageDigest.isEqual(adminKey.getBytes(StandardCharsets.UTF_8),
                requestKey.getBytes(StandardCharsets.UTF_8));
    }
}
