package kr.codesqaud.cafe.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.codesqaud.cafe.common.filter.RequestContext;
import kr.codesqaud.cafe.controller.dto.CommentSaveResponse;
import kr.codesqaud.cafe.controller.dto.req.CommentRequest;
import kr.codesqaud.cafe.exception.NoAuthorizationException;
import kr.codesqaud.cafe.service.CommentService;

@WebMvcTest(CommentController.class)
@Import(RequestContext.class)
public class CommentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper mapper;

	@MockBean
	private CommentService commentService;

	@DisplayName("[POST] 댓글을 작성 정보가 주어질 때 댓글을 작성하면 작성한 댓글 정보가 응답으로 돌아온다.")
	@Test
	void givenReplyRequest_whenReply_thenReturnReplyResponse() throws Exception {
		// given
		CommentRequest request = new CommentRequest(1L, "댓글의 내용이랍니다~");
		CommentSaveResponse response = new CommentSaveResponse(1L, "댓글의 내용이랍니다~", LocalDateTime.now(), "bruni", 1L);
		given(commentService.reply(any(CommentRequest.class), anyString())).willReturn(response);

		// when & then
		mockMvc.perform(post("/comments")
			                .contentType(MediaType.APPLICATION_JSON)
			                .content(mapper.writeValueAsString(request))
			                .sessionAttr("sessionedUser", "bruni"))
			.andExpect(status().isCreated())
			.andExpect(content().string(mapper.writeValueAsString(response)))
			.andDo(print());

		then(commentService).should().reply(any(CommentRequest.class), anyString());
	}

	@DisplayName("[POST] 세션이 없을 때 댓글 작성 요청을 하면 로그인 페이지로 리다이렉트 된다.")
	@Test
	void givenNoSession_whenReply_thenRedirectsLoginPage() throws Exception {
		// given
		CommentRequest request = new CommentRequest(1L, "댓글의 내용이랍니다~");
		CommentSaveResponse response = new CommentSaveResponse(1L, "댓글의 내용이랍니다~", LocalDateTime.now(), "bruni", 1L);

		given(commentService.reply(any(CommentRequest.class), anyString())).willReturn(response);

		// when & then
		mockMvc.perform(post("/comments")
			                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
			                .content(mapper.writeValueAsString(request)))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/user/login"))
			.andDo(print());

		then(commentService).shouldHaveNoInteractions();
	}

	@DisplayName("[DELETE] 게시글 아이다가 주어질 때 댓글 삭제 요청을 하면 게시글 상세화면으로 리다이렉트된다.")
	@Test
	void givenArticleId_whenDelete_thenRedirectsArticleDetailsPage() throws Exception {
		// given
		willDoNothing().given(commentService).checkDeleteCommentPermission(anyLong(), anyString());
		willDoNothing().given(commentService).deleteById(anyLong());

		// when & then
		mockMvc.perform(delete("/comments/1")
			                .contentType(MediaType.APPLICATION_JSON)
			                .sessionAttr("sessionedUser", "bruni"))
			.andExpect(status().isOk())
			.andDo(print());

		assertAll(
			() -> then(commentService).should().checkDeleteCommentPermission(1L, "bruni"),
			() -> then(commentService).should().deleteById(1L)
		);
	}

	@DisplayName("[DELETE] 세션이 없을 때 댓글 삭제 요청을 하면 로그인페이지로 리다이렉트 된다.")
	@Test
	void givenNoSession_whenDelete_thenRedirectsLoginPage() throws Exception {
		// given
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("articleId", "1");

		// when & then
		mockMvc.perform(delete("/comments/1")
			                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
			                .params(body))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/user/login"))
			.andDo(print());

		then(commentService).shouldHaveNoInteractions();
	}

	@DisplayName("[DELETE] 댓글의 작성자와 현재 로그인된 사용자가 일치하지 않을 때 댓글 삭제 요청을 하면 에러뷰가 반환된다.")
	@Test
	void givenNotEqualsWriter_whenDelete_thenReturnsErrorView() throws Exception {
		// given
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("articleId", "1");

		willThrow(NoAuthorizationException.class).given(commentService)
			.checkDeleteCommentPermission(anyLong(), anyString());
		willDoNothing().given(commentService).deleteById(anyLong());

		// when & then
		mockMvc.perform(delete("/comments/1")
			                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
			                .params(body)
			                .sessionAttr("sessionedUser", "bruni"))
			.andExpect(status().isOk())
			.andExpect(view().name("error"))
			.andDo(print());

		assertAll(
			() -> then(commentService).should().checkDeleteCommentPermission(1L, "bruni"),
			() -> then(commentService).should(never()).deleteById(anyLong())
		);
	}
}
