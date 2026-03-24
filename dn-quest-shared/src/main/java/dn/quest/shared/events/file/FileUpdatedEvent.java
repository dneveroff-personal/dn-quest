package dn.quest.shared.events.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Событие обновления файла
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUpdatedEvent {

    private String eventId;
    private Long fileId;
    private Long userId;
    private String fileName;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String filePath;
    private String storageType;
}
