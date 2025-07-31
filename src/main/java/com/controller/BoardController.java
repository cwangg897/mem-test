package com.controller;

import com.dto.BoardRequestDto;
import com.dto.BoardResponseDto;
import com.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 게시글 생성
    @PostMapping
    public ResponseEntity<BoardResponseDto> createBoard(@RequestBody BoardRequestDto requestDto) {
        BoardResponseDto responseDto = boardService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 게시글 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDto> getBoard(@PathVariable Long id) {
        BoardResponseDto responseDto = boardService.findById(id);
        return ResponseEntity.ok(responseDto);
    }

    // 전체 게시글 조회 (페이징)
    @GetMapping
    public ResponseEntity<Page<BoardResponseDto>> getAllBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BoardResponseDto> boards = boardService.findAll(page, size);
        return ResponseEntity.ok(boards);
    }

    // 전체 게시글 조회 (리스트)
    @GetMapping("/list")
    public ResponseEntity<List<BoardResponseDto>> getAllBoardsList() {
        List<BoardResponseDto> boards = boardService.findAll();
        return ResponseEntity.ok(boards);
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<BoardResponseDto> updateBoard(
            @PathVariable Long id, 
            @RequestBody BoardRequestDto requestDto) {
        BoardResponseDto responseDto = boardService.update(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 제목으로 검색
    @GetMapping("/search/title")
    public ResponseEntity<List<BoardResponseDto>> searchByTitle(@RequestParam String title) {
        List<BoardResponseDto> boards = boardService.findByTitle(title);
        return ResponseEntity.ok(boards);
    }

    // 작성자로 검색
    @GetMapping("/search/author")
    public ResponseEntity<List<BoardResponseDto>> searchByAuthor(@RequestParam String author) {
        List<BoardResponseDto> boards = boardService.findByAuthor(author);
        return ResponseEntity.ok(boards);
    }

    // 키워드로 검색 (제목 또는 내용)
    @GetMapping("/search")
    public ResponseEntity<List<BoardResponseDto>> searchByKeyword(@RequestParam String keyword) {
        List<BoardResponseDto> boards = boardService.searchByKeyword(keyword);
        return ResponseEntity.ok(boards);
    }

    // 최신 게시글 10개 조회
    @GetMapping("/latest")
    public ResponseEntity<List<BoardResponseDto>> getLatestBoards() {
        List<BoardResponseDto> boards = boardService.findLatest();
        return ResponseEntity.ok(boards);
    }

    // 성능 테스트용: 대량 데이터 생성
    @PostMapping("/bulk")
    public ResponseEntity<String> createBulkBoards(@RequestParam(defaultValue = "1000") int count) {
        for (int i = 1; i <= count; i++) {
            BoardRequestDto requestDto = new BoardRequestDto();
            requestDto.setTitle("테스트 게시글 " + i);
            requestDto.setContent("이것은 테스트용 게시글 내용입니다. 번호: " + i);
            requestDto.setAuthor("테스터" + (i % 10 + 1));
            boardService.create(requestDto);
        }
        return ResponseEntity.ok(count + "개의 게시글이 생성되었습니다.");
    }
}
