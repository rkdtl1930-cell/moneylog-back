package com.kbs.backend.controller;

import com.kbs.backend.domain.Member;
import com.kbs.backend.dto.PageRequestDTO;
import com.kbs.backend.dto.PageResponseDTO;
import com.kbs.backend.dto.TransactionDTO;
import com.kbs.backend.repository.MemberRepository;
import com.kbs.backend.security.UserPrincipal;
import com.kbs.backend.service.MemberService;
import com.kbs.backend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

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

    @PutMapping
    public ResponseEntity<Void> modify(@RequestBody TransactionDTO transactionDTO) {
        transactionService.modify(transactionDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
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

    @GetMapping("/category")
    public ResponseEntity<List<Map<String,Object>>> categoryStats(@AuthenticationPrincipal UserPrincipal user){
        Long mid = user.getId();
        return ResponseEntity.ok(transactionService.getCategoryStats(mid));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<Map<String,Object>>> monthlyStats(@AuthenticationPrincipal UserPrincipal user){
        Long mid = user.getId();
        return ResponseEntity.ok(transactionService.getMonthlyStats(mid));
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
            @AuthenticationPrincipal UserPrincipal user
    ) {
        // 안전 상한(툴 limit 최대 50)
        int safeLimit = Math.min(Math.max(limit, 1), 50);

        // 기존 getList(PageRequestDTO, mid)를 재사용 (size 최소 10 제약이 있어서 우회)
        PageRequestDTO pr = PageRequestDTO.builder()
                .page(1)
                .size(Math.max(10, safeLimit))
                .build();

        PageResponseDTO<TransactionDTO> page = transactionService.getList(pr, user.getId());

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
}
