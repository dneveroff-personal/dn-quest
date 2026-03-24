package dn.quest.shared.events.file;

import dn.quest.shared.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Событие удаления файла
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FileDeletedEvent extends BaseEvent {

    private Long fileId;
    private Long userId;
    private String fileName;
}
