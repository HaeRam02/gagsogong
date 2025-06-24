package com.example.gagso.Documents.repository;

import com.example.gagso.Documents.models.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByDocumentDocID(String docID);

}