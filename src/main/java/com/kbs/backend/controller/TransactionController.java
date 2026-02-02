package com.kbs.backend.controller;

import com.kbs.backend.domain.Member;
import com.kbs.backend.domain.Transaction;
import com.kbs.backend.domain.TransactionType;
import com.kbs.backend.dto.*;
import com.kbs.backend.exception.AmbiguousTransactionException;
import com.kbs.backend.repository.MemberRepository;
import com.kbs.backend.security.UserPrincipal;
import com.kbs.backend.service.MemberService;
import com.kbs.backend.service.TransactionService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private MemberRepository memberRepository;

    @PostMapping
    public ResponseEntity<Long> register(@RequestBody TransactionDTO transactionDTO, @AuthenticationPrincipal UserPrincipal userprincipal) {
        System.out.println("UserPrincipal 객체: " + userprincipal);
        System.out.println("ID: " + (userprincipal != null ? userprincipal.getId() : null));
        if (userprincipal == null || userprincipal.getId() == null) {
            return ResponseEntity.status(401).build(); // 인증 안된 경우
        }

        // Member 객체 가져오기
        Member member = memberRepository.findById(userprincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));        Long id = transactionService.register(transactionDTO, member);
        return ResponseEntity.ok(id);
    }

    @PostMapping("/batch")
    public ResponseEntity<?> registerBatch(@RequestBody TransactionBatchRequestDTO req, @AuthenticationPrincipal UserPrincipal userPrincipal){
        if(userPrincipal == null || userPrincipal.getId() == null) return ResponseEntity.status(401).build();
        Member member = memberRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(req.getTransactions() == null || req.getTransactions().isEmpty()) return ResponseEntity.badRequest().body(Map.of("message","등록할 내역이 없습니다."));
        BatchRegisterResultDTO result = transactionService.registerBatch(req.getTransactions(), member);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> get(@PathVariable Long id) {
        TransactionDTO transactionDTO = transactionService.get(id);
        if(transactionDTO == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transactionDTO);
    }

    @GetMapping("/member/{mid}")
    public ResponseEntity<PageResponseDTO<TransactionDTO>> getList(@PathVariable Long mid, PageRequestDTO pageRequestDTO) {
        PageResponseDTO<TransactionDTO> list = transactionService.getList(pageRequestDTO ,mid);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/member/{mid}/date")
    public ResponseEntity<PageResponseDTO<TransactionDTO>> getByDate(@PathVariable Long mid, @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, PageRequestDTO pageRequestDTO) {
        return ResponseEntity.ok(transactionService.getListByDate(pageRequestDTO, mid, date));
    }

    @GetMapping("/member/{mid}/period")
    public ResponseEntity<PageResponseDTO<TransactionDTO>> getByPeriod(@PathVariable Long mid,
                                                            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                                                       PageRequestDTO pageRequestDTO) {
        return ResponseEntity.ok(transactionService.getListByPeriod(pageRequestDTO,mid, start, end));
    }

    // 회원 인증을 위해 수정함.
    @PutMapping
    public ResponseEntity<Void> modify(
            @RequestBody TransactionDTO transactionDTO,
            @AuthenticationPrincipal UserPrincipal userprincipal
    ) {
        if (userprincipal == null || userprincipal.getId() == null) {
            return ResponseEntity.status(401).build();
        }
        if (transactionDTO.getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        TransactionDTO existing = transactionService.get(transactionDTO.getId());
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (existing.getMid() == null || !existing.getMid().equals(userprincipal.getId())) {
            return ResponseEntity.status(403).build();
        }

        transactionService.modify(transactionDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userprincipal
    ) {
        if (userprincipal == null || userprincipal.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        TransactionDTO dto = transactionService.get(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        // 본인 소유 거래만 삭제 가능
        if (dto.getMid() == null || !dto.getMid().equals(userprincipal.getId())) {
            return ResponseEntity.status(403).build();
        }

        transactionService.remove(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/member/{mid}/month")
    public ResponseEntity<PageResponseDTO<TransactionDTO>> getByMonth(@PathVariable Long mid, @RequestParam(value = "month", required = false) String monthStr, PageRequestDTO pageRequestDTO) {
        YearMonth yearMonth;
        if(monthStr == null || monthStr.isEmpty()){
            yearMonth = YearMonth.now();
        } else {
            yearMonth = YearMonth.parse(monthStr);
        }

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        PageResponseDTO<TransactionDTO> list = transactionService.getListByPeriod(pageRequestDTO, mid, start, end);

        return ResponseEntity.ok(list);
    }
    
    // 원래는 통계용으로 쓸려고 했으나 안 쓰게 됨
    @GetMapping("/category")
    public ResponseEntity<List<Map<String,Object>>> categoryStats(@AuthenticationPrincipal UserPrincipal user){
        Long mid = user.getId();
        return ResponseEntity.ok(transactionService.getCategoryStats(mid));
    }

    // 위와 동일
    @GetMapping("/monthly")
    public ResponseEntity<List<Map<String,Object>>> monthlyStats(@AuthenticationPrincipal UserPrincipal user){
        Long mid = user.getId();
        return ResponseEntity.ok(transactionService.getMonthlyStats(mid));
    }

    // 요일별 평균 지출(기간: month/year) 중 최댓값 반환 (chat-router)
    // scope=month: month=YYYY-MM (없으면 현재 월)
    // scope=year : year=YYYY (없으면 현재 연)
    @GetMapping("/weekday/top")
    public ResponseEntity<TopWeekdayAvgExpenseDTO> topWeekdayAvgExpense(
            @RequestParam("scope") String scope,
            @RequestParam(value = "month", required = false) String monthStr,
            @RequestParam(value = "year", required = false) String yearStr,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Long mid = user.getId();

        LocalDate start;
        LocalDate end;

        if ("month".equalsIgnoreCase(scope)) {
            YearMonth ym = (monthStr == null || monthStr.isBlank()) ? YearMonth.now() : YearMonth.parse(monthStr);
            start = ym.atDay(1);
            end = ym.atEndOfMonth();
        } else if ("year".equalsIgnoreCase(scope)) {
            int y = (yearStr == null || yearStr.isBlank()) ? Year.now().getValue() : Integer.parseInt(yearStr);
            start = LocalDate.of(y, 1, 1);
            end = LocalDate.of(y, 12, 31);
        } else {
            return ResponseEntity.badRequest().build();
        }

        // 미래 날짜 제외: 분모(count) 왜곡 방지
        LocalDate today = LocalDate.now();

        // 조회 시작일이 오늘보다 미래면(예: 미래 달/미래 연도 조회) → 오늘까지 데이터가 없으므로 0 반환
        if (start.isAfter(today)) {
            return ResponseEntity.ok(
                    TopWeekdayAvgExpenseDTO.builder()
                            .weekday("")
                            .avgAmount(0.0)
                            .build()
            );
        }

        // end가 오늘보다 미래면 today로 절단(이번 달/올해 질문에서 미래 날짜 제외)
        if (end.isAfter(today)) {
            end = today;
        }


        return ResponseEntity.ok(transactionService.getTopWeekdayAvgExpense(mid, start, end));
    }


    //특정 한 하루의 내역만 보게 하는 서비스
    @GetMapping("/member/{mid}/day")
    public ResponseEntity<PageResponseDTO<TransactionDTO>> getBySingleDay(
            @PathVariable Long mid,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            PageRequestDTO pageRequestDTO) {
        return ResponseEntity.ok(transactionService.getListBySingleDay(pageRequestDTO, mid, date));
    }

    //최근 내역 조회(chat-router)
    @GetMapping("/recent")
    public ResponseEntity<List<TransactionDTO>> recent(
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(value = "type", required = false ) String type,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        // 안전 상한(툴 limit 최대 50)
        int safeLimit = Math.min(Math.max(limit, 1), 50);

        // 기존 getList(PageRequestDTO, mid)를 재사용 (size 최소 10 제약이 있어서 우회)
        PageRequestDTO pr = PageRequestDTO.builder()
                .page(1)
                .size(Math.max(10, safeLimit))
                .build();

        PageResponseDTO<TransactionDTO> page = transactionService.getListByAI(pr, user.getId(), type);

        // 정확히 limit 개수만 반환
        List<TransactionDTO> sliced = page.getDtoList()
                .stream()
                .limit(safeLimit)
                .toList();

        return ResponseEntity.ok(sliced);
    }

    // 기간 내의 자신의 가계부 내역을 삭제하는 api
    @DeleteMapping("/delete/period")
    public ResponseEntity<Void> deleteByPeriod(@AuthenticationPrincipal UserPrincipal user,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end){
        Long mid = user.getId();
        transactionService.removeByPeriod(mid, start, end);
        return ResponseEntity.ok().build();
    }

    // 챗봇용 삭제
    @PostMapping("/chat/delete")
    public ResponseEntity<?> deleteByChat(@AuthenticationPrincipal UserPrincipal user,
                                             @RequestBody DeleteByAIRequest req){
        if(user == null || user.getId() == null){
            return ResponseEntity.status(401).build();
        }
        try{
            transactionService.removeByAI(user.getId(), req.getDate(), req.getAmount(), req.getMemo(), req.getType());
            return ResponseEntity.ok().build();
        }catch(AmbiguousTransactionException e){
            int idx =1;
            List<TransactionCandidateDTO> candidateDTOS = new ArrayList<>();
            for(Transaction tx : e.getCandidates()){
                TransactionCandidateDTO dto = TransactionCandidateDTO.builder()
                        .id(tx.getId())
                        .date(tx.getDate())
                        .amount(tx.getAmount())
                        .memo(tx.getMemo())
                        .category(tx.getCategory())
                        .number(idx++)
                        .type(tx.getType().name())
                        .build();
                candidateDTOS.add(dto);
            }
            transactionService.storeDeleteCandidates(user.getId(), candidateDTOS);
            List<Map<String, Object>> candidates = new ArrayList<>();
            for(TransactionCandidateDTO dto : candidateDTOS){
                Map<String,Object> m = new HashMap<>();
                m.put("number", dto.getNumber());
                m.put("date", dto.getDate());
                m.put("amount", dto.getAmount());
                m.put("memo", dto.getMemo());
                m.put("category", dto.getCategory());
                candidates.add(m);
            }
            return ResponseEntity.status(409).body(candidates);
        }catch(IllegalArgumentException e){
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/chat/delete/confirm")
    public ResponseEntity<?> confirmDelete(@AuthenticationPrincipal UserPrincipal user,
                                           @RequestBody DeleteConfirmRequest req){
        transactionService.confirmDeleteByChat(user.getId(), req.getSelectedIndexes(), req.getType());
        return ResponseEntity.ok().body(Map.of("message", "선택된 항목 삭제 완료"));
    }

    // 챗봇용 수정
    @PostMapping("/chat/update")
    public ResponseEntity<?> updateByChat(@AuthenticationPrincipal UserPrincipal user,
                                          @RequestBody ModifyByAIRequest req) {

        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        System.out.println("REQ = date=" + req.getDate()
                + ", amount=" + req.getAmount()
                + ", memo=" + req.getMemo());

        List<TransactionCandidateDTO> candidates =
                transactionService.getUpdateCandidates(
                        user.getId(),
                        req.getDate(),
                        req.getAmount(),
                        req.getMemo(),
                        req.getType()
                );

        if (candidates.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "지출내역이 없습니다."));
        }


        // 후보 여러 개든 1개든 전부 내려줌 (UX 일관성)
        List<Map<String, Object>> response = new ArrayList<>();

        for (TransactionCandidateDTO dto : candidates) {
            Map<String, Object> m = new HashMap<>();
            m.put("number", dto.getNumber());
            m.put("date", dto.getDate());
            m.put("amount", dto.getAmount());
            m.put("memo", dto.getMemo());
            response.add(m);
        }

        Map<String,Object> body = new HashMap<>();
        body.put("candidates", response);

        System.out.println(">>> CONTROLLER candidates size = " + candidates.size());

        return ResponseEntity.status(409).body(body);
    }

    @PostMapping("/chat/update/confirm")
    public ResponseEntity<?> confirmUpdateByChat(@AuthenticationPrincipal UserPrincipal user,
                                                 @RequestBody UpdateConfirmRequest req) {

        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        if (req.getCandidateIndex() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "후보 번호가 없습니다."));
        }
        if(req.getType() == null){
            return ResponseEntity.badRequest()
                    .body(Map.of("message","거래 타입이 없습니다."));
        }
        if (req.getNewData() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "수정할 내용이 없습니다."));
        }

        try {
            transactionService.confirmUpdateByChat(
                    user.getId(),
                    req.getType(),
                    req.getCandidateIndex(),
                    req.getNewData()
            );
            return ResponseEntity.ok(Map.of("message", "수정 완료"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@RequestParam("period")String period, @RequestParam("type")String type, @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate baseDate, @AuthenticationPrincipal UserPrincipal user){
        if(user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }
        LocalDate resolvedBaseDate = (baseDate != null) ? baseDate : LocalDate.now();
        TransactionType txType;
        try{
            txType = TransactionType.valueOf(type.toUpperCase());
        }catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("message", "타입은 반드시 EXPENSE거나 INCOME이어야 합니다."));
        }
        LocalDate start;
        LocalDate end;
        switch (period.toLowerCase()){
            case "day" -> {
                start = resolvedBaseDate;
                end = resolvedBaseDate;
            }
            case "week" -> {
                start = resolvedBaseDate.with(java.time.DayOfWeek.MONDAY);
                end = resolvedBaseDate.with(java.time.DayOfWeek.SUNDAY);
            }
            case "month" -> {
                start = resolvedBaseDate.withDayOfMonth(1);
                end = resolvedBaseDate.withDayOfMonth(
                        resolvedBaseDate.lengthOfMonth()
                );
            }
            case "year" -> {
                start = resolvedBaseDate.withDayOfYear(1);
                end = resolvedBaseDate.withDayOfYear(
                        resolvedBaseDate.lengthOfYear()
                );
            }
            default -> {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "기간은 반드시 day/week/month/year 중 하나여야 합니다."
                        ));
            }
        }
        if (start.isAfter(end)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "잘못된 날짜 범위입니다."));
        }
        Long totalAmount = transactionService.getTotalAmount(user.getId(), txType, start, end);
        Map<String,Object> response = new HashMap<>();
        response.put("period", period);
        response.put("totalAmount", totalAmount);
        response.put("type",txType.name());
        response.put("start", start);
        response.put("end", end);
        response.put("baseDate", resolvedBaseDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-expense-category")
    public ResponseEntity<Map<String, Object>> getTopExpenseCategory(
            @RequestParam("period") String period,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate baseDate,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        LocalDate resolvedBaseDate =
                (baseDate != null) ? baseDate : LocalDate.now();

        LocalDate start;
        LocalDate end;

        switch (period.toLowerCase()) {
            case "day" -> {
                start = resolvedBaseDate;
                end = resolvedBaseDate;
            }
            case "week" -> {
                start = resolvedBaseDate.with(java.time.DayOfWeek.MONDAY);
                end = resolvedBaseDate.with(java.time.DayOfWeek.SUNDAY);
            }
            case "month" -> {
                start = resolvedBaseDate.withDayOfMonth(1);
                end = resolvedBaseDate.withDayOfMonth(
                        resolvedBaseDate.lengthOfMonth()
                );
            }
            case "year" -> {
                start = resolvedBaseDate.withDayOfYear(1);
                end = resolvedBaseDate.withDayOfYear(
                        resolvedBaseDate.lengthOfYear()
                );
            }
            default -> {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "period는 week 또는 month만 지원합니다."
                        ));
            }
        }

        Map<String, Object> result =
                transactionService.getTopCategoryByPeriod(
                        user.getId(),
                        start,
                        end
                );

        Map<String, Object> response = new HashMap<>();
        response.put("period", period);
        response.put("category", result.get("category"));
        response.put("totalAmount", result.get("totalAmount"));
        response.put("start", start);
        response.put("end", end);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/latest")
    public ResponseEntity<?> removeLatest(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            TransactionDTO deleted =
                    transactionService.removeLatest(user.getId());
            return ResponseEntity.ok(deleted);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/latest")
    public ResponseEntity<?> modifyLatest(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody TransactionDTO newData
    ) {
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            TransactionDTO updated =
                    transactionService.modifyLatest(user.getId(), newData);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }


    public static class DeleteByAIRequest {
        @Getter
        @Setter
        private LocalDate date;

        @Getter
        @Setter
        private int amount;

        @Getter
        @Setter
        private String memo;

        @Getter
        @Setter
        private String type;
    }


    public static class DeleteConfirmRequest {
        @Getter
        @Setter
        private List<Integer> selectedIndexes;
        @Getter
        @Setter
        private TransactionDTO newData;
        @Getter
        @Setter
        private  String type;
    }

    public static class UpdateConfirmRequest {
        @Getter @Setter
        private Integer candidateIndex;

        @Getter @Setter
        private TransactionDTO newData; // date / amount / memo 중 일부만 채워짐

        @Getter @Setter
        private String type;
    }

}
