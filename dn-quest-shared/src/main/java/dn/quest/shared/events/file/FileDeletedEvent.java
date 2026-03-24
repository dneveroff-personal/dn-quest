package dn.quest.shared.events.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Событие удаления файла
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDeletedEvent {

    private String eventId;
    private Long fileId;
    private Long userId;
    private String fileName;
}
