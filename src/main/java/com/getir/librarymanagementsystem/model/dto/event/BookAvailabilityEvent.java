package com.getir.librarymanagementsystem.model.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookAvailabilityEvent {
    private Long bookId;
    private boolean available;
}
