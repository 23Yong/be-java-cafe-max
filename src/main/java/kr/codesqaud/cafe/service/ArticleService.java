package kr.codesqaud.cafe.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.codesqaud.cafe.controller.dto.ArticleDetails;
import kr.codesqaud.cafe.controller.dto.ArticleResponse;
import kr.codesqaud.cafe.controller.dto.ArticleWithCommentCount;
import kr.codesqaud.cafe.controller.dto.CommentResponse;
import kr.codesqaud.cafe.controller.dto.req.ArticleEditRequest;
import kr.codesqaud.cafe.controller.dto.req.PostingRequest;
import kr.codesqaud.cafe.domain.article.Article;
import kr.codesqaud.cafe.domain.comment.Comment;
import kr.codesqaud.cafe.exception.InvalidOperationException;
import kr.codesqaud.cafe.exception.NoAuthorizationException;
import kr.codesqaud.cafe.exception.NotFoundException;
import kr.codesqaud.cafe.repository.ArticleRepository;
import kr.codesqaud.cafe.repository.CommentRepository;
import kr.codesqaud.cafe.service.paging.Page;
import kr.codesqaud.cafe.service.paging.Pageable;

@Transactional(readOnly = true)
@Service
public class ArticleService {

	private final ArticleRepository articleRepository;
	private final CommentRepository commentRepository;

	public ArticleService(ArticleRepository articleRepository, CommentRepository commentRepository) {
		this.articleRepository = articleRepository;
		this.commentRepository = commentRepository;
	}

	@Transactional
	public void post(final PostingRequest request, final String userId) {
		articleRepository.save(Article.of(userId, request.getTitle(), request.getContents()));
	}

	public ArticleDetails getArticleDetails(final Long id) {
		Article article = articleRepository.findById(id)
			.orElseThrow(() -> new NotFoundException(String.format("%d번 게시글을 찾을 수 없습니다.", id)));
		List<Comment> comments = commentRepository.findAllByArticleId(id);
		return new ArticleDetails(ArticleResponse.from(article), comments.stream()
			.map(CommentResponse::from)
			.collect(Collectors.toUnmodifiableList()));
	}

	public Page<ArticleWithCommentCount> getArticlesWithCommentCount(final Pageable pageable) {
		List<ArticleWithCommentCount> articles = articleRepository.findAllArticleWithCommentCount(pageable)
			.stream()
			.collect(Collectors.toUnmodifiableList());
		Long totalCount = articleRepository.countAllArticles();
		return new Page<>(articles, totalCount);
	}

	public void validateHasAuthorization(final Long articleId, final String userId) {
		articleRepository.findById(articleId)
			.filter(article -> article.isSameWriter(userId))
			.orElseThrow(NoAuthorizationException::new);
	}

	@Transactional
	public void editArticle(final Long articleId, final ArticleEditRequest request) {
		Article savedArticle = articleRepository.findById(articleId)
			.orElseThrow(() -> new NotFoundException(String.format("%d번 게시글을 찾을 수 없습니다.", articleId)));

		savedArticle.editArticle(request.getTitle(), request.getContent());
		articleRepository.update(savedArticle);
	}

	@Transactional
	public void deleteArticle(final Long articleId) {
		articleRepository.findById(articleId)
			.orElseThrow(() -> new NotFoundException(String.format("%d번 게시글을 찾을 수 없습니다.", articleId)));
		if (articleRepository.isPossibleDeleteById(articleId)) {
			articleRepository.deleteById(articleId);
			return;
		}
		throw new InvalidOperationException(articleId);
	}
}
