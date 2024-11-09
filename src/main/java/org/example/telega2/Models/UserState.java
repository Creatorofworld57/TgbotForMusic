package org.example.telega2.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.example.telega2.Utils.Constants;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Getter
@Setter
@Data
@Entity
public class UserState {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_tracks", joinColumns = @JoinColumn(name = "user_id"))
    List<Audio> tracks;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_answers", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "idOfTrack")   // Название столбца для ключа в таблице
    @Column(name = "trackName")
    Map<Long, String> answer = new HashMap<>();
    Constants constants;
    long chatId;
    String current;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


}
