package kr.codesqaud.cafe.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import kr.codesqaud.cafe.controller.dto.req.ReplyRequest;
import kr.codesqaud.cafe.exception.NoAuthorizationException;
import kr.codesqaud.cafe.service.ArticleCommentService;

@WebMvcTest(ArticleCommentController.class)
public class ArticleCommentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ArticleCommentService articleCommentService;

	@DisplayName("[POST] 댓글 작성 - 정상호출")
	@Test
	void givenReplyRequest_whenReply_thenRedirectsArticleDetailsPage() throws Exception {
		// given
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("articleId", "1");
		body.add("content", "댓글의 내용이랍니다~");

		willDoNothing().given(articleCommentService).reply(any(ReplyRequest.class), anyString());

		// when & then
		mockMvc.perform(post("/comments")
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.params(body)
							.sessionAttr("sessionedUser", "bruni"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/articles/1"))
			.andDo(print());

		then(articleCommentService).should().reply(any(ReplyRequest.class), anyString());
	}

	@DisplayName("[POST] 댓글 작성 - 세션이 없을 때 로그인 페이지로 리다이렉트 된다.")
	@Test
	void givenNoSession_whenReply_thenRedirectsLoginPage() throws Exception {
		// given
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("articleId", "1");
		body.add("content", "댓글의 내용이랍니다~");

		willDoNothing().given(articleCommentService).reply(any(ReplyRequest.class), anyString());

		// when & then
		mockMvc.perform(post("/comments")
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.params(body))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/user/login"))
			.andDo(print());

		then(articleCommentService).shouldHaveNoInteractions();
	}

	@DisplayName("[DELETE] 댓글 삭제 - 정상호출")
	@Test
	void givenArticleId_whenDelete_thenRedirectsArticleDetailsPage() throws Exception {
		// given
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("articleId", "1");

		willDoNothing().given(articleCommentService).validateHasAuthorization(anyLong(), anyString());
		willDoNothing().given(articleCommentService).deleteById(anyLong());

		// when & then
		mockMvc.perform(delete("/comments/1")
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.params(body)
							.sessionAttr("sessionedUser", "bruni"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/articles/1"))
			.andDo(print());

		assertAll(
			() -> then(articleCommentService).should().validateHasAuthorization(1L, "bruni"),
			() -> then(articleCommentService).should().deleteById(1L)
		);
	}

	@DisplayName("[DELETE] 댓글 삭제 - 세션이 없을 때 로그인페이지로 리다이렉트 된다.")
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

		then(articleCommentService).shouldHaveNoInteractions();
	}

	@DisplayName("[DELETE] 댓글 삭제 - 댓글의 작성자와 현재 로그인된 사용자가 일치하지 않으면 에러뷰가 반환된다.")
	@Test
	void givenNotEqualsWriter_whenDelete_thenReturnsErrorView() throws Exception {
		// given
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("articleId", "1");

		willThrow(NoAuthorizationException.class).given(articleCommentService)
			.validateHasAuthorization(anyLong(), anyString());
		willDoNothing().given(articleCommentService).deleteById(anyLong());

		// when & then
		mockMvc.perform(delete("/comments/1")
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.params(body)
							.sessionAttr("sessionedUser", "bruni"))
			.andExpect(status().isOk())
			.andExpect(view().name("error"))
			.andDo(print());

		assertAll(
			() -> then(articleCommentService).should().validateHasAuthorization(1L, "bruni"),
			() -> then(articleCommentService).should(never()).deleteById(anyLong())
		);
	}
}