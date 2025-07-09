package tn.gti.E_Transfert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.gti.E_Transfert.dto.request.TransferRequestRequestDTO;
import tn.gti.E_Transfert.dto.response.DocumentResponseDTO;
import tn.gti.E_Transfert.dto.response.TransferRequestResponseDTO;
import tn.gti.E_Transfert.entity.Beneficiary;
import tn.gti.E_Transfert.entity.Document;
import tn.gti.E_Transfert.entity.TransferRequest;
import tn.gti.E_Transfert.enums.TransferStatus;
import tn.gti.E_Transfert.enums.TransferType;
import tn.gti.E_Transfert.exception.TransferException;
import tn.gti.E_Transfert.repository.BeneficiaryRepository;
import tn.gti.E_Transfert.repository.DocumentRepository;
import tn.gti.E_Transfert.repository.TransferRequestRepository;

import jakarta.annotation.PostConstruct;
import tn.gti.E_Transfert.repository.TransferRequestSpecification;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TransferRequestService {

    private final TransferRequestRepository transferRequestRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final DocumentRepository documentRepository;
    private final ModelMapper modelMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath);
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", e.getMessage(), e);
            throw new TransferException("Failed to initialize upload directory", e);
        }
    }
    public List<TransferRequestResponseDTO> searchTransferRequests(Long userId, String commissionAccountNumber,
                                                                   TransferType transferType, TransferStatus status, BigDecimal amount) {
        log.info("Searching transfer requests with criteria: userId={}, commissionAccountNumber={}, transferType={}, status={}, amount={}",
                userId, commissionAccountNumber, transferType, status, amount);
        try {
            return transferRequestRepository.findAll(TransferRequestSpecification.searchByCriteria(userId, commissionAccountNumber, transferType, status, amount))
                    .stream()
                    .map(transferRequest -> modelMapper.map(transferRequest, TransferRequestResponseDTO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search transfer requests: {}", e.getMessage(), e);
            throw new TransferException("Failed to search transfer requests", e);
        }
    }

    public TransferRequestResponseDTO createTransferRequestWithDocument(TransferRequestRequestDTO requestDTO, MultipartFile documentFile) {
        log.info("Creating transfer request with document for user ID: {}", requestDTO.getUserId());
        validateCreateDTO(requestDTO);
        try {
            TransferRequest transferRequest = modelMapper.map(requestDTO, TransferRequest.class);
            Beneficiary beneficiary = transferRequest.getBeneficiary();
            beneficiary = beneficiaryRepository.save(beneficiary);
            transferRequest.setBeneficiary(beneficiary);
            transferRequest.setStatus(TransferStatus.PENDING);
            transferRequest.setCreatedAt(LocalDateTime.now());
            TransferRequest saved = transferRequestRepository.save(transferRequest);
            Document document = createDocumentFromFile(documentFile, saved);
            saved.getDocuments().add(document);
            saved = transferRequestRepository.save(saved);
            return modelMapper.map(saved, TransferRequestResponseDTO.class);
        } catch (IOException e) {
            log.error("Failed to create transfer request with document: {}", e.getMessage(), e);
            throw new TransferException("Failed to create transfer request with document", e);
        } catch (Exception e) {
            log.error("Unexpected error creating transfer request: {}", e.getMessage(), e);
            throw new TransferException("Unexpected error creating transfer request", e);
        }
    }
    public TransferRequestResponseDTO createTransferRequestWithDefaultDocument(TransferRequestRequestDTO requestDTO) {
        log.info("Creating transfer request with default document for user ID: {}", requestDTO.getUserId());
        validateCreateDTO(requestDTO);
        try {
            log.info("Mapping DTO to TransferRequest: {}", requestDTO);
            TransferRequest transferRequest = modelMapper.map(requestDTO, TransferRequest.class);
            log.info("Mapped TransferRequest: {}", transferRequest);
            Beneficiary beneficiary = transferRequest.getBeneficiary();
            log.info("Saving Beneficiary: {}", beneficiary);
            beneficiary = beneficiaryRepository.save(beneficiary);
            log.info("Saved Beneficiary: {}", beneficiary);
            transferRequest.setBeneficiary(beneficiary);
            transferRequest.setStatus(TransferStatus.PENDING);
            transferRequest.setCreatedAt(LocalDateTime.now());
            log.info("Saving TransferRequest: {}", transferRequest);
            TransferRequest saved = transferRequestRepository.save(transferRequest);
            log.info("Saved TransferRequest: {}", saved);
            Document defaultDocument = createDefaultDocument(saved);
            saved.getDocuments().add(defaultDocument);
            saved = transferRequestRepository.save(saved);
            return modelMapper.map(saved, TransferRequestResponseDTO.class);
        } catch (IOException e) {
            log.error("Failed to create transfer request with default document: {}", e.getMessage(), e);
            throw new TransferException("Failed to create transfer request with default document", e);
        } catch (Exception e) {
            log.error("Unexpected error creating transfer request: {}", e.getMessage(), e);
            throw new TransferException("Unexpected error creating transfer request", e);
        }
    }

    public TransferRequestResponseDTO updateTransferRequest(Long id, TransferRequestRequestDTO requestDTO) {
        log.info("Updating transfer request with ID: {}", id);
        log.debug("Request DTO: {}", requestDTO);
        if (requestDTO.getBeneficiaryId() == null) {
            throw new TransferException("Beneficiary ID is required for update");
        }
        try {
            TransferRequest existing = transferRequestRepository.findById(id)
                    .orElseThrow(() -> new TransferException("Transfer request not found with ID: " + id));
            log.debug("Existing TransferRequest: {}", existing);
            if (existing.getStatus() != TransferStatus.PENDING && existing.getStatus() != TransferStatus.INFO_REQUESTED) {
                throw new TransferException("Cannot update transfer request with status: " + existing.getStatus());
            }
            modelMapper.map(requestDTO, existing);
            log.debug("After mapping DTO to existing: {}", existing);
            Beneficiary beneficiary = beneficiaryRepository.findById(requestDTO.getBeneficiaryId())
                    .orElseThrow(() -> new TransferException("Beneficiary not found with ID: " + requestDTO.getBeneficiaryId()));
            existing.setBeneficiary(beneficiary);
            TransferRequest saved = transferRequestRepository.save(existing);
            log.debug("Saved TransferRequest: {}", saved);
            return modelMapper.map(saved, TransferRequestResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to update transfer request with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to update transfer request with ID: " + id, e);
        }
    }
    private void validateCreateDTO(TransferRequestRequestDTO requestDTO) {
        if (requestDTO.getBeneficiary() == null) {
            throw new TransferException("Beneficiary is required");
        }
        if (requestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransferException("Amount must be greater than 0");
        }
    }

    private Document createDocumentFromFile(MultipartFile file, TransferRequest transferRequest) throws IOException {
        List<String> allowedTypes = List.of("application/pdf", "image/png", "image/jpeg");
        if (file == null || file.isEmpty()) {
            log.error("File is null or empty");
            throw new TransferException("Uploaded file is empty or not provided");
        }
        if (!allowedTypes.contains(file.getContentType())) {
            log.error("Invalid file type: {}. Allowed types are: {}", file.getContentType(), allowedTypes);
            throw new TransferException("Invalid file type. Only PDF, PNG, and JPEG are allowed");
        }
        Path uploadPath = Paths.get(uploadDir);
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setFilePath(filePath.toString());
        document.setTransferRequest(transferRequest);
        document.setUploadDate(LocalDateTime.now());
        return documentRepository.save(document);
    }

    private Document createDefaultDocument(TransferRequest transferRequest) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        String defaultContent = "Default document for Transfer Request #" + transferRequest.getIdTransferRequest() +
                "\nCreated on: " + LocalDateTime.now() +
                "\nTransfer Type: " + transferRequest.getTransferType() +
                "\nAmount: " + transferRequest.getAmount() + " " + transferRequest.getCurrency() +
                "\nBeneficiary: " + transferRequest.getBeneficiary().getName();
        String fileName = "default_document_" + UUID.randomUUID() + ".txt";
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, defaultContent.getBytes());
        Document document = new Document();
        document.setFileName("Default Transfer Document.txt");
        document.setFileType("text/plain");
        document.setFilePath(filePath.toString());
        document.setTransferRequest(transferRequest);
        document.setUploadDate(LocalDateTime.now());
        return documentRepository.save(document);
    }

    public List<TransferRequestResponseDTO> getAllTransferRequests() {
        log.info("Retrieving all transfer requests");
        try {
            return transferRequestRepository.findAllWithDetails().stream()
                    .map(transferRequest -> modelMapper.map(transferRequest, TransferRequestResponseDTO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to retrieve all transfer requests: {}", e.getMessage(), e);
            throw new TransferException("Failed to retrieve all transfer requests", e);
        }
    }

    public TransferRequestResponseDTO getTransferRequestById(Long id) {
        log.info("Retrieving transfer request with ID: {}", id);
        try {
            TransferRequest transferRequest = transferRequestRepository.findById(id)
                    .orElseThrow(() -> new TransferException("Transfer request not found with ID: " + id));
            return modelMapper.map(transferRequest, TransferRequestResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to retrieve transfer request with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to retrieve transfer request with ID: " + id, e);
        }
    }

    public void deleteTransferRequest(Long id) {
        log.info("Deleting transfer request with ID: {}", id);
        try {
            if (!transferRequestRepository.existsById(id)) {
                throw new TransferException("Transfer request not found with ID: " + id);
            }
            transferRequestRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Failed to delete transfer request with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to delete transfer request with ID: " + id, e);
        }
    }

    public TransferRequestResponseDTO validateTransferRequest(Long id, Long validatorId) {
        log.info("Validating transfer request with ID: {} by validator: {}", id, validatorId);
        try {
            return transferRequestRepository.findById(id)
                    .map(transfer -> {
                        transfer.setStatus(TransferStatus.VALIDATED);
                        transfer.setValidatorId(validatorId);
                        transfer.setValidatedAt(LocalDateTime.now());
                        TransferRequest saved = transferRequestRepository.save(transfer);
                        return modelMapper.map(saved, TransferRequestResponseDTO.class);
                    })
                    .orElseThrow(() -> new TransferException("Transfer request not found with ID: " + id));
        } catch (Exception e) {
            log.error("Failed to validate transfer request with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to validate transfer request with ID: " + id, e);
        }
    }

    public TransferRequestResponseDTO rejectTransferRequest(Long id, Long validatorId) {
        log.info("Rejecting transfer request with ID: {} by validator: {}", id, validatorId);
        try {
            return transferRequestRepository.findById(id)
                    .map(transfer -> {
                        transfer.setStatus(TransferStatus.REJECTED);
                        transfer.setValidatorId(validatorId);
                        transfer.setValidatedAt(LocalDateTime.now());
                        TransferRequest saved = transferRequestRepository.save(transfer);
                        return modelMapper.map(saved, TransferRequestResponseDTO.class);
                    })
                    .orElseThrow(() -> new TransferException("Transfer request not found with ID: " + id));
        } catch (Exception e) {
            log.error("Failed to reject transfer request with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to reject transfer request with ID: " + id, e);
        }
    }

    public TransferRequestResponseDTO requestAdditionalInfo(Long id, Long validatorId) {
        log.info("Requesting additional info for transfer request with ID: {} by validator: {}", id, validatorId);
        try {
            return transferRequestRepository.findById(id)
                    .map(transfer -> {
                        transfer.setStatus(TransferStatus.INFO_REQUESTED);
                        transfer.setValidatorId(validatorId);
                        TransferRequest saved = transferRequestRepository.save(transfer);
                        return modelMapper.map(saved, TransferRequestResponseDTO.class);
                    })
                    .orElseThrow(() -> new TransferException("Transfer request not found with ID: " + id));
        } catch (Exception e) {
            log.error("Failed to request additional info for transfer request with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to request additional information for transfer request with ID: " + id, e);
        }
    }

    public List<DocumentResponseDTO> getDocuments(Long id) {
        log.info("Retrieving documents for transfer request with ID: {}", id);
        try {
            TransferRequest transferRequest = transferRequestRepository.findById(id)
                    .orElseThrow(() -> new TransferException("Transfer request not found with ID: " + id));
            return transferRequest.getDocuments().stream()
                    .map(document -> modelMapper.map(document, DocumentResponseDTO.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to retrieve documents for transfer request with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to retrieve documents for transfer request with ID: " + id, e);
        }
    }

    @Transactional
    public void uploadDocument(Long id, MultipartFile file) {
        log.info("Uploading document for transfer request with ID: {}", id);
        try {
            TransferRequest transferRequest = transferRequestRepository.findById(id)
                    .orElseThrow(() -> new TransferException("Transfer request not found with ID: " + id));
            Document document = createDocumentFromFile(file, transferRequest);
            transferRequest.getDocuments().add(document);
            transferRequestRepository.save(transferRequest);
        } catch (IOException e) {
            log.error("Failed to upload document for transfer request with ID {}: {}", id, e.getMessage(), e);
            throw new TransferException("Failed to store file", e);
        }
    }

    @Transactional
    public void deleteDocument(Long transferRequestId, Long documentId) {
        log.info("Deleting document with ID: {} for transfer request with ID: {}", documentId, transferRequestId);
        try {
            TransferRequest transferRequest = transferRequestRepository.findById(transferRequestId)
                    .orElseThrow(() -> new TransferException("Transfer request not found with ID: " + transferRequestId));
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new TransferException("Document not found with ID: " + documentId));
            if (!document.getTransferRequest().getIdTransferRequest().equals(transferRequestId)) {
                throw new TransferException("Document with ID: " + documentId + " does not belong to transfer request with ID: " + transferRequestId);
            }
            transferRequest.getDocuments().remove(document);
            documentRepository.delete(document);
            try {
                Files.deleteIfExists(Paths.get(document.getFilePath()));
                log.info("Deleted file from filesystem: {}", document.getFilePath());
            } catch (IOException e) {
                log.warn("Failed to delete file from filesystem: {}", document.getFilePath(), e);
            }
        } catch (Exception e) {
            log.error("Failed to delete document with ID {} for transfer request with ID {}: {}", documentId, transferRequestId, e.getMessage(), e);
            throw new TransferException("Failed to delete document with ID: " + documentId, e);
        }
    }
}