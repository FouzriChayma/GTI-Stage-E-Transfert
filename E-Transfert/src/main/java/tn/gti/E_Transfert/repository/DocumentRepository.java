// src/main/java/tn/gti/E_Transfert/repository/DocumentRepository.java
package tn.gti.E_Transfert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.gti.E_Transfert.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}