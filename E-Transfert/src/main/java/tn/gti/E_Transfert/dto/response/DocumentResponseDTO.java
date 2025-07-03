package tn.gti.E_Transfert.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponseDTO {
    Long idDocument;
    String fileName;
    String fileType;
    String filePath;
    LocalDateTime uploadDate;
}