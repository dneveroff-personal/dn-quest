package dn.quest.shared.events.file;

import dn.quest.shared.events.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Событие удаления файла
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FileDeletedEvent extends BaseEvent {

    private Long fileId;
    private Long userId;
    private String fileName;
}
