package kr.codesqaud.cafe.repository;

import java.util.List;
import java.util.Optional;

import kr.codesqaud.cafe.domain.article.Article;

public interface ArticleRepository {

	Optional<Article> save(Article article);

	List<Article> findAll();

	Optional<Article> findById(Long id);
}