package com.xrdj.iris.repository;

import com.xrdj.iris.model.FileArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileArchiveRepository extends JpaRepository<FileArchive, Long> {
    boolean existsByFileName(String fileName);
    Optional<FileArchive> findByFileName(String fileName);
}
