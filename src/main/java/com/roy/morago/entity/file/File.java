package com.roy.morago.entity.file;

import com.roy.morago.entity.BaseEntity;
import com.roy.morago.enums.FilePurpose;
import com.roy.morago.enums.FileStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "files")
public class File extends BaseEntity {
    @Column(nullable = false)
    private String fileName;
    @Column(nullable = false)
    private String filePath;
    @Column(nullable = false)
    private String fileType;
    @Column(nullable = false)
    private Long fileSize;
    @Column(nullable = false)
    private FilePurpose filePurpose;
    @Column
    private FileStatus fileStatus;

    public void activate() {
        this.fileStatus = FileStatus.ACTIVE;
    }
}
