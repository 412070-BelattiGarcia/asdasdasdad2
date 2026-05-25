package ar.edu.utn.frc.tup.piii.engine.ports;

import java.util.List;

public interface RandomizerPort {
    <T> T shuffleAndPick(List<T> items, int count);
    int nextInt(int bound);
}
