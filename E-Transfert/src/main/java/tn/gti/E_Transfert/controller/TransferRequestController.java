package tn.gti.E_Transfert.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.gti.E_Transfert.dto.request.TransferRequestRequestDTO;
import tn.gti.E_Transfert.dto.response.DocumentResponseDTO;
import tn.gti.E_Transfert.dto.response.TransferRequestResponseDTO;
import tn.gti.E_Transfert.enums.TransferStatus;
import tn.gti.E_Transfert.enums.TransferType;
import tn.gti.E_Transfert.service.TransferRequestService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transfer-requests")
@RequiredArgsConstructor
public class TransferRequestController {

    private final TransferRequestService transferRequestService;

    @GetMapping("/search")
    public ResponseEntity<List<TransferRequestResponseDTO>> searchTransferRequests(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String commissionAccountNumber,
            @RequestParam(required = false) TransferType transferType,
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(required = false) BigDecimal amount) {
        List<TransferRequestResponseDTO> list = transferRequestService.searchTransferRequests(userId, commissionAccountNumber, transferType, status, amount);
        return list.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(list);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<TransferRequestResponseDTO> createTransferRequest(
            @RequestPart("transferRequest") @Valid TransferRequestRequestDTO requestDTO,
            @RequestPart("document") MultipartFile document) {
        TransferRequestResponseDTO created = transferRequestService.createTransferRequestWithDocument(requestDTO, document);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PostMapping("/json")
    public ResponseEntity<TransferRequestResponseDTO> createTransferRequestJson(
            @Valid @RequestBody TransferRequestRequestDTO requestDTO) {
        TransferRequestResponseDTO created = transferRequestService.createTransferRequestWithoutDocument(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @PostMapping("/no-document")
    public ResponseEntity<TransferRequestResponseDTO> createTransferRequestNoDocument(
            @Valid @RequestBody TransferRequestRequestDTO requestDTO) {
        TransferRequestResponseDTO created = transferRequestService.createTransferRequestWithoutDocument(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @PutMapping("/{id}")
    public ResponseEntity<TransferRequestResponseDTO> updateTransferRequest(
            @PathVariable Long id, @Valid @RequestBody TransferRequestRequestDTO requestDTO) {
        return ResponseEntity.ok(transferRequestService.updateTransferRequest(id, requestDTO));
    }

    @GetMapping
    public ResponseEntity<List<TransferRequestResponseDTO>> getAllTransferRequests() {
        List<TransferRequestResponseDTO> list = transferRequestService.getAllTransferRequests();
        return list.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferRequestResponseDTO> getTransferRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(transferRequestService.getTransferRequestById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransferRequest(@PathVariable Long id) {
        transferRequestService.deleteTransferRequest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<TransferRequestResponseDTO> validateTransfer(
            @PathVariable Long id, @RequestParam Long validatorId) {
        return ResponseEntity.ok(transferRequestService.validateTransferRequest(id, validatorId));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<TransferRequestResponseDTO> rejectTransfer(
            @PathVariable Long id, @RequestParam Long validatorId) {
        return ResponseEntity.ok(transferRequestService.rejectTransferRequest(id, validatorId));
    }

    @PostMapping("/{id}/request-info")
    public ResponseEntity<TransferRequestResponseDTO> requestInfo(
            @PathVariable Long id, @RequestParam Long validatorId) {
        return ResponseEntity.ok(transferRequestService.requestAdditionalInfo(id, validatorId));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<String> uploadDocument(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        transferRequestService.uploadDocument(id, file);
        return ResponseEntity.ok("Document uploaded successfully");
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<DocumentResponseDTO>> getDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(transferRequestService.getDocuments(id));
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, @PathVariable Long documentId) {
        transferRequestService.deleteDocument(id, documentId);
        return ResponseEntity.noContent().build();
    }

}