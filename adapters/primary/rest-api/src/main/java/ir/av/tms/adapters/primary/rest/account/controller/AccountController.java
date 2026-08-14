package ir.av.tms.adapters.primary.rest.account.controller;

import ir.av.tms.adapters.primary.rest.account.request.CreditAccountRestRequest;
import ir.av.tms.adapters.primary.rest.account.request.DebitAccountRestRequest;
import ir.av.tms.adapters.primary.rest.account.request.OpenAccountRestRequest;
import ir.av.tms.adapters.primary.rest.account.request.TransferAccountRestRequest;
import ir.av.tms.adapters.primary.rest.account.response.AccountDataRestResponse;
import ir.av.tms.adapters.primary.rest.base.BaseResponse;
import ir.av.tms.application.delivery.AccountService;
import ir.av.tms.application.delivery.dto.AccountDto;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping()
    public ResponseEntity<?> openAccount(@RequestBody OpenAccountRestRequest request) {
        UUID accountId = accountService.openAccount(request.getName());
        return ResponseEntity.created(URI.create("/accounts/".concat(accountId.toString()))).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<AccountDataRestResponse>> get(@PathVariable("id") UUID id) {
        AccountDto accountDto = accountService.get(id);
        AccountDataRestResponse response = new AccountDataRestResponse();
        response.setAccountId(id);
        response.setName(accountDto.name());
        response.setBalance(accountDto.balance());
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<BaseResponse<?>> credit(@PathVariable("id") UUID id,
                                                  @RequestBody CreditAccountRestRequest request,
                                                  @NotBlank @RequestHeader("Idempotency-Key") String idempotencyKey) {
        accountService.credit(id, request.getAmount(), idempotencyKey);
        return ResponseEntity.ok(BaseResponse.defaultSuccess());
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<BaseResponse<?>> debit(@PathVariable("id") UUID id,
                                                 @RequestBody DebitAccountRestRequest request,
                                                 @NotBlank @RequestHeader("Idempotency-Key") String idempotencyKey) {
        accountService.debit(id, request.getAmount(), idempotencyKey);
        return ResponseEntity.ok(BaseResponse.defaultSuccess());
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<BaseResponse<?>> transfer(@PathVariable("id") UUID id,
                                                    @RequestBody TransferAccountRestRequest request,
                                                    @NotBlank @RequestHeader("Idempotency-Key") String idempotencyKey) {
        accountService.transfer(id, request.getDestinationAccountId(), request.getAmount(), idempotencyKey);
        return ResponseEntity.ok(BaseResponse.defaultSuccess());
    }

}
