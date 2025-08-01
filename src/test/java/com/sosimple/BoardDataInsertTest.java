package com.sosimple;

import com.dto.BoardRequestDto;
import com.service.BoardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class BoardDataInsertTest {

    @Autowired
    private BoardService boardService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final Random random = new Random();

    @Test
    @DisplayName("멀티스레드로 80_000개 게시글 데이터 생성")
    void insertBoardDataWithMultiThread() throws InterruptedException {
        int totalCount = 80_000;
        CountDownLatch latch = new CountDownLatch(totalCount);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    BoardRequestDto request = generateRandomBoardRequest(index);
                    boardService.create(request);

                    if (index % 100 == 0) {
                        System.out.println("진행률: " + index + "/" + totalCount);
                    }
                } catch (Exception e) {
                    System.err.println("데이터 생성 실패 - index: " + index + ", error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        System.out.println("=== 데이터 생성 완료 ===");
        System.out.println("총 처리 시간: " + executionTime + "ms");
        System.out.println("평균 처리 시간: " + (executionTime / (double) totalCount) + "ms per insert");
    }

    private BoardRequestDto generateRandomBoardRequest(int index) {
        String[] titlePrefixes = {"테스트", "성능", "부하", "벤치마크", "샘플", "더미", "데이터", "검증"};
        String[] contentWords = {
            "MySQL", "메모리", "성능", "테스트", "데이터베이스", "최적화", "인덱스", "쿼리",
            "트랜잭션", "커넥션", "풀링", "캐시", "백엔드", "API", "서버", "개발"
        };

        // 랜덤 제목 생성
        String title = titlePrefixes[random.nextInt(titlePrefixes.length)] +
                      " 게시글 " + (index + 1) + " - " +
                      System.currentTimeMillis();

        // 랜덤 내용 생성 (50-200 단어)
        StringBuilder content = new StringBuilder();
        int wordCount = 50 + random.nextInt(151); // 50~200개 단어

        for (int i = 0; i < wordCount; i++) {
            content.append(contentWords[random.nextInt(contentWords.length)]);
            if (i < wordCount - 1) {
                content.append(" ");
            }

            // 가끔 문장 끝에 마침표 추가
            if (random.nextInt(10) == 0) {
                content.append(". ");
            }
        }

        // 작성자도 랜덤하게
        String author = "user" + random.nextInt(100);

        BoardRequestDto request = new BoardRequestDto();
        request.setTitle(title);
        request.setContent(content.toString());
        request.setAuthor(author);

        return request;
    }

    @AfterEach
    void cleanup() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
