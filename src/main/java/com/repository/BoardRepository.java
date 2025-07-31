package com.repository;

import com.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 제목으로 검색
    List<Board> findByTitleContaining(String title);

    // 작성자로 검색
    List<Board> findByAuthor(String author);

    // 제목 또는 내용으로 검색
    @Query("SELECT b FROM Board b WHERE b.title LIKE %:keyword% OR b.content LIKE %:keyword%")
    List<Board> findByTitleOrContentContaining(@Param("keyword") String keyword);

    // 페이징 처리된 전체 조회 (최신순)
    Page<Board> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 최신 게시글 N개 조회
    List<Board> findTop10ByOrderByCreatedAtDesc();
}
