package com.service;

import com.dto.BoardRequestDto;
import com.dto.BoardResponseDto;
import com.entity.Board;
import com.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    // 게시글 생성
    @Transactional
    public BoardResponseDto create(BoardRequestDto requestDto) {
        Board board = requestDto.toEntity();
        Board savedBoard = boardRepository.save(board);
        return new BoardResponseDto(savedBoard);
    }

    // 게시글 단건 조회
    public BoardResponseDto findById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + id));
        return new BoardResponseDto(board);
    }

    // 전체 게시글 조회 (페이징)
    public Page<BoardResponseDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return boardRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(BoardResponseDto::new);
    }

    // 전체 게시글 조회 (리스트)
    public List<BoardResponseDto> findAll() {
        return boardRepository.findAll(Sort.by("createdAt").descending())
                .stream()
                .map(BoardResponseDto::new)
                .collect(Collectors.toList());
    }

    // 게시글 수정
    @Transactional
    public BoardResponseDto update(Long id, BoardRequestDto requestDto) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + id));
        
        board.update(requestDto.getTitle(), requestDto.getContent());
        return new BoardResponseDto(board);
    }

    // 게시글 삭제
    @Transactional
    public void delete(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. ID: " + id));
        boardRepository.delete(board);
    }

    // 제목으로 검색
    public List<BoardResponseDto> findByTitle(String title) {
        return boardRepository.findByTitleContaining(title)
                .stream()
                .map(BoardResponseDto::new)
                .collect(Collectors.toList());
    }

    // 작성자로 검색
    public List<BoardResponseDto> findByAuthor(String author) {
        return boardRepository.findByAuthor(author)
                .stream()
                .map(BoardResponseDto::new)
                .collect(Collectors.toList());
    }

    // 키워드로 검색 (제목 또는 내용)
    public List<BoardResponseDto> searchByKeyword(String keyword) {
        return boardRepository.findByTitleOrContentContaining(keyword)
                .stream()
                .map(BoardResponseDto::new)
                .collect(Collectors.toList());
    }

    // 최신 게시글 10개 조회
    public List<BoardResponseDto> findLatest() {
        return boardRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(BoardResponseDto::new)
                .collect(Collectors.toList());
    }
}
