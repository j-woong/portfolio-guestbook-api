package com.jwoong.guestbook.guestbook;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestbookRepository extends JpaRepository<GuestbookEntry, Long> {

    // 최신 글부터 조회한다. Page 대신 Slice를 써서 전체 개수를 세는 count 쿼리를 보내지 않는다
    Slice<GuestbookEntry> findAllByOrderByIdDesc(Pageable pageable);
}
