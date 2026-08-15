package com.xrdj.iris.repository;

import com.xrdj.iris.model.FileArchive;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileArchiveRepository extends JpaRepository<FileArchive, Long> {
    boolean existsByFileName(String fileName);

    Optional<FileArchive> findByFileName(String fileName);
}
