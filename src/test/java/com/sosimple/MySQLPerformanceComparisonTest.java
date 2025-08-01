package com.sosimple;

import com.dto.BoardRequestDto;
import com.dto.BoardResponseDto;
import com.service.BoardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class MySQLPerformanceComparisonTest {

    @Autowired
    private BoardService boardService;

    private final Random random = new Random();

    @Test
    @DisplayName("📝 INSERT 성능 테스트 - 80,000개 데이터 생성")
    void insertPerformanceTest() throws InterruptedException {
        int totalCount = 80_000;
        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(totalCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        System.out.println("\n🚀 === INSERT 성능 테스트 시작 ===");
        System.out.println("📊 총 데이터 수: " + String.format("%,d", totalCount) + "개");
        System.out.println("🧵 스레드 수: " + threadCount + "개");
        System.out.println("🔄 스레드당 처리량: " + String.format("%,d", totalCount/threadCount) + "개");
        
        long startTime = System.currentTimeMillis();
        
        // 진행률 표시를 위한 별도 스레드
        Thread progressThread = new Thread(() -> {
            try {
                while (latch.getCount() > 0) {
                    int completed = totalCount - (int)latch.getCount();
                    int progress = (int)((completed * 100.0) / totalCount);
                    System.out.printf("\r⏳ 진행률: %d%% (%,d/%,d) - 성공: %,d, 실패: %,d", 
                        progress, completed, totalCount, successCount.get(), errorCount.get());
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        progressThread.start();
        
        // 데이터 INSERT 작업
        for (int i = 0; i < totalCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    BoardRequestDto request = generateRandomBoardRequest(index);
                    boardService.create(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("\n❌ 에러 발생 - index: " + index + ", error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        progressThread.interrupt();
        executorService.shutdown();
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        System.out.println("\n\n✅ === INSERT 성능 테스트 완료 ===");
        System.out.println("⏱️  총 실행 시간: " + String.format("%,d", executionTime) + "ms (" + (executionTime/1000.0) + "초)");
        System.out.println("✅ 성공: " + String.format("%,d", successCount.get()) + "개");
        System.out.println("❌ 실패: " + String.format("%,d", errorCount.get()) + "개");
        System.out.println("📈 초당 처리량: " + String.format("%.2f", (successCount.get() * 1000.0) / executionTime) + " TPS");
        System.out.println("⚡ 평균 처리 시간: " + String.format("%.2f", executionTime / (double) successCount.get()) + "ms per insert");
        
        if (errorCount.get() > 0) {
            System.out.println("⚠️  에러율: " + String.format("%.2f", (errorCount.get() * 100.0) / totalCount) + "%");
        }
    }

    @Test
    @DisplayName("📖 READ 성능 테스트 - 대량 조회")
    void readPerformanceTest() throws InterruptedException {
        System.out.println("\n📚 === READ 성능 테스트 시작 ===");
        
        // 1. 전체 개수 조회
        long startTime = System.currentTimeMillis();
        List<BoardResponseDto> allBoards = boardService.findAll();
        long countTime = System.currentTimeMillis() - startTime;
        
        System.out.println("📊 전체 데이터 수: " + String.format("%,d", allBoards.size()) + "개");
        System.out.println("⏱️  전체 조회 시간: " + countTime + "ms");
        
        // 2. 페이징 조회 성능 테스트
        System.out.println("\n🔍 페이징 조회 성능 테스트:");
        int[] pageSizes = {10, 50, 100, 500, 1000};
        
        for (int pageSize : pageSizes) {
            startTime = System.currentTimeMillis();
            Page<BoardResponseDto> page = boardService.findAll(0, pageSize);
            long pageTime = System.currentTimeMillis() - startTime;
            
            System.out.printf("📄 페이지 크기 %,d: %dms (실제 조회: %d개)\n", 
                pageSize, pageTime, page.getContent().size());
        }
        
        // 3. 제목 검색 성능 테스트
        System.out.println("\n🔎 검색 성능 테스트:");
        String[] searchKeywords = {"테스트", "성능", "데이터", "MySQL", "벤치마크"};
        
        for (String keyword : searchKeywords) {
            startTime = System.currentTimeMillis();
            List<BoardResponseDto> searchResults = boardService.findByTitle(keyword);
            long searchTime = System.currentTimeMillis() - startTime;
            
            System.out.printf("🔍 '%s' 검색: %dms (결과: %,d개)\n", 
                keyword, searchTime, searchResults.size());
        }
        
        // 4. 멀티스레드 읽기 성능 테스트
        System.out.println("\n🧵 멀티스레드 읽기 성능 테스트:");
        
        int readThreadCount = 10;
        int readsPerThread = 100;
        ExecutorService readExecutor = Executors.newFixedThreadPool(readThreadCount);
        CountDownLatch readLatch = new CountDownLatch(readThreadCount * readsPerThread);
        AtomicInteger readSuccessCount = new AtomicInteger(0);
        
        startTime = System.currentTimeMillis();
        
        for (int i = 0; i < readThreadCount * readsPerThread; i++) {
            readExecutor.submit(() -> {
                try {
                    // 랜덤한 페이지 조회
                    int randomPage = random.nextInt(100);
                    boardService.findAll(randomPage, 20);
                    readSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("읽기 에러: " + e.getMessage());
                } finally {
                    readLatch.countDown();
                }
            });
        }
        
        readLatch.await();
        readExecutor.shutdown();
        
        long readEndTime = System.currentTimeMillis();
        long readExecutionTime = readEndTime - startTime;
        int totalReads = readSuccessCount.get();
        
        System.out.println("\n📊 멀티스레드 읽기 결과:");
        System.out.println("🧵 스레드 수: " + readThreadCount);
        System.out.println("📖 총 읽기 수: " + String.format("%,d", totalReads) + "번");
        System.out.println("⏱️  총 시간: " + readExecutionTime + "ms");
        System.out.println("📈 초당 읽기: " + String.format("%.2f", (totalReads * 1000.0) / readExecutionTime) + " QPS");
        System.out.println("⚡ 평균 읽기 시간: " + String.format("%.2f", readExecutionTime / (double) totalReads) + "ms per read");
        
        System.out.println("\n✅ === READ 성능 테스트 완료 ===");
    }

    @Test
    @DisplayName("🔄 혼합 성능 테스트 - INSERT + READ 동시 실행")
    void mixedPerformanceTest() throws InterruptedException {
        System.out.println("\n🔄 === 혼합 성능 테스트 시작 ===");
        
        int insertCount = 10_000;
        int readCount = 5_000;
        int totalOperations = insertCount + readCount;
        
        ExecutorService mixedExecutor = Executors.newFixedThreadPool(15);
        CountDownLatch mixedLatch = new CountDownLatch(totalOperations);
        AtomicInteger insertSuccess = new AtomicInteger(0);
        AtomicInteger readSuccess = new AtomicInteger(0);
        
        System.out.println("📝 INSERT 작업: " + String.format("%,d", insertCount) + "개");
        System.out.println("📖 READ 작업: " + String.format("%,d", readCount) + "개");
        
        long startTime = System.currentTimeMillis();
        
        // INSERT 작업들
        for (int i = 0; i < insertCount; i++) {
            final int index = i;
            mixedExecutor.submit(() -> {
                try {
                    BoardRequestDto request = generateRandomBoardRequest(index + 100_000);
                    boardService.create(request);
                    insertSuccess.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("INSERT 에러: " + e.getMessage());
                } finally {
                    mixedLatch.countDown();
                }
            });
        }
        
        // READ 작업들
        for (int i = 0; i < readCount; i++) {
            mixedExecutor.submit(() -> {
                try {
                    int randomPage = random.nextInt(50);
                    boardService.findAll(randomPage, 50);
                    readSuccess.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("READ 에러: " + e.getMessage());
                } finally {
                    mixedLatch.countDown();
                }
            });
        }
        
        mixedLatch.await();
        mixedExecutor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        System.out.println("\n📊 혼합 성능 테스트 결과:");
        System.out.println("⏱️  총 실행 시간: " + executionTime + "ms (" + (executionTime/1000.0) + "초)");
        System.out.println("✅ INSERT 성공: " + String.format("%,d", insertSuccess.get()) + "개");
        System.out.println("✅ READ 성공: " + String.format("%,d", readSuccess.get()) + "개");
        System.out.println("📈 총 TPS: " + String.format("%.2f", ((insertSuccess.get() + readSuccess.get()) * 1000.0) / executionTime));
        
        System.out.println("\n✅ === 혼합 성능 테스트 완료 ===");
    }

    private BoardRequestDto generateRandomBoardRequest(int index) {
        String[] titlePrefixes = {"성능테스트", "부하테스트", "벤치마크", "MySQL", "데이터베이스", "최적화", "인덱싱", "쿼리"};
        String[] contentWords = {
            "MySQL", "메모리", "성능", "테스트", "데이터베이스", "최적화", "인덱스", "쿼리",
            "트랜잭션", "커넥션", "풀링", "캐시", "백엔드", "API", "서버", "개발", "Spring", "Boot",
            "JPA", "Hibernate", "Docker", "벤치마크", "부하", "스트레스", "TPS", "QPS"
        };
        
        // 랜덤 제목 생성
        String title = titlePrefixes[random.nextInt(titlePrefixes.length)] + 
                      " " + (index + 1) + " - " + 
                      System.currentTimeMillis() + "-" + random.nextInt(1000);
        
        // 랜덤 내용 생성 (100-300 단어)
        StringBuilder content = new StringBuilder();
        int wordCount = 100 + random.nextInt(201);
        
        for (int i = 0; i < wordCount; i++) {
            content.append(contentWords[random.nextInt(contentWords.length)]);
            if (i < wordCount - 1) {
                content.append(" ");
            }
            
            // 문장 구분
            if (random.nextInt(15) == 0) {
                content.append(". ");
            }
        }
        
        // 작성자 랜덤 생성
        String author = "testuser" + random.nextInt(500);
        
        BoardRequestDto request = new BoardRequestDto();
        request.setTitle(title);
        request.setContent(content.toString());
        request.setAuthor(author);
        
        return request;
    }
}
