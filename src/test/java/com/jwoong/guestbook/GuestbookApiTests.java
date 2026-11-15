package com.jwoong.guestbook;

import com.jwoong.guestbook.guestbook.GuestbookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.admin-key=test-admin-key")
@AutoConfigureMockMvc
class GuestbookApiTests {

    private static final String URL = "/api/guestbook";
    // 테스트마다 다른 IP를 써서 작성 대기 시간이 서로 영향을 주지 않게 한다
    private static final AtomicInteger IP = new AtomicInteger(1);

    @Autowired
    MockMvc mockMvc;
    @Autowired
    GuestbookRepository guestbookRepository;

    @BeforeEach
    void setUp() {
        guestbookRepository.deleteAll();
    }

    @Test
    void 글을_남기면_201과_저장된_글을_반환하고_목록_맨_위에_보인다() throws Exception {
        write(newIp(), "첫 손님", "잘 보고 갑니다", false).andExpect(status().isCreated());
        write(newIp(), "  지웅  ", "  두 번째 글  ", true)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").value("지웅"))
                .andExpect(jsonPath("$.message").value("두 번째 글"))
                .andExpect(jsonPath("$.foundCat").value(true));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries", hasSize(2)))
                .andExpect(jsonPath("$.entries[0].nickname").value("지웅"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 닉네임이나_메시지가_비어_있으면_400과_필드별_메시지를_반환한다() throws Exception {
        write(newIp(), " ", "", false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nickname").exists())
                .andExpect(jsonPath("$.errors.message").exists());
    }

    @Test
    void 메시지가_140자를_넘으면_400을_반환한다() throws Exception {
        write(newIp(), "손님", "가".repeat(141), false).andExpect(status().isBadRequest());
    }

    @Test
    void 같은_IP가_대기_시간_안에_다시_쓰면_429를_반환한다() throws Exception {
        String ip = newIp();
        write(ip, "손님", "첫 글", false).andExpect(status().isCreated());
        write(ip, "손님", "바로 또 쓰기", false).andExpect(status().isTooManyRequests());
    }

    @Test
    void 검증에_실패한_요청은_작성_횟수로_세지_않는다() throws Exception {
        String ip = newIp();
        write(ip, "", "", false).andExpect(status().isBadRequest());
        write(ip, "손님", "제대로 쓴 글", false).andExpect(status().isCreated());
    }

    @Test
    void 페이지_크기는_30개로_제한하고_다음_페이지_여부를_알려준다() throws Exception {
        for (int i = 0; i < 31; i++) {
            write(newIp(), "손님" + i, "글 " + i, false).andExpect(status().isCreated());
        }
        mockMvc.perform(get(URL).param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries", hasSize(30)))
                .andExpect(jsonPath("$.hasNext").value(true));
        mockMvc.perform(get(URL).param("size", "30").param("page", "1"))
                .andExpect(jsonPath("$.entries", hasSize(1)))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 관리자_키가_없거나_틀리면_삭제할_수_없고_맞으면_삭제된다() throws Exception {
        write(newIp(), "손님", "지워질 글", false).andExpect(status().isCreated());
        Long id = guestbookRepository.findAll().get(0).getId();

        mockMvc.perform(delete(URL + "/" + id)).andExpect(status().isForbidden());
        mockMvc.perform(delete(URL + "/" + id).header("X-Admin-Key", "wrong")).andExpect(status().isForbidden());
        mockMvc.perform(delete(URL + "/" + id).header("X-Admin-Key", "test-admin-key")).andExpect(status().isNoContent());
        mockMvc.perform(delete(URL + "/" + id).header("X-Admin-Key", "test-admin-key")).andExpect(status().isNotFound());
    }

    private ResultActions write(String ip, String nickname, String message, boolean foundCat) throws Exception {
        String body = "{\"nickname\":\"%s\",\"message\":\"%s\",\"foundCat\":%s}".formatted(nickname, message, foundCat);
        return mockMvc.perform(post(URL).header("X-Forwarded-For", ip)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String newIp() {
        return "10.0.0." + IP.getAndIncrement();
    }
}
