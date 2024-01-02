package org.example;

import org.example.generated.dto.DogDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.example.generated.api.DogsApiDelegate;

import java.util.ArrayList;
import java.util.List;

@Service
public class DogService implements DogsApiDelegate {
    private static final List<DogDto> DOGS = new ArrayList<>(List.of(
            new DogDto().id(1L).name("Fido").age(3).breed("Labrador"),
            new DogDto().id(2L).name("Rex").age(5).breed("German Shepherd"),
            new DogDto().id(3L).name("Spot").age(2).breed("Dalmatian")
    ));

    @Override
    public ResponseEntity<List<DogDto>> getAllDogs() {
        return ResponseEntity.ok(DOGS);
    }

    @Override
    public ResponseEntity<Void> deleteDogById(Long id) {
        if (DOGS.removeIf(dog -> dog.getId().equals(id))) {
            return ResponseEntity.noContent().build();
        } else {
            throw new DogNotFoundException("Dog with id " + id + " not found");
        }
    }
}
