package org.example.telega2.Models;



import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.io.Serializable;


@Data
@Getter
@Setter
public class Audio implements Serializable {
    private Long id;
    private String name;
    private String Author;
    private byte[] imagesc;
    public Audio() {

    }
}
