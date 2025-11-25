package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.VendaJogadorResponseDTO;
import com.futmaneger.application.usecase.transferencias.ComprarJogadorUseCase;
import com.futmaneger.application.usecase.transferencias.VenderJogadorUseCase;
import com.futmaneger.presentation.request.ComprarJogadorRequest;
import com.futmaneger.presentation.response.ComprarJogadorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transferencias")
public class TransferenciasController {
    private final ComprarJogadorUseCase comprarJogadorUseCase;
    private final VenderJogadorUseCase venderJogadorUseCase;

    public TransferenciasController(ComprarJogadorUseCase comprarJogadorUseCase,
                                    VenderJogadorUseCase venderJogadorUseCase) {
        this.comprarJogadorUseCase = comprarJogadorUseCase;
        this.venderJogadorUseCase = venderJogadorUseCase;
    }

    @PostMapping("/comprar")
    public ResponseEntity<ComprarJogadorResponse> comprar(@RequestBody ComprarJogadorRequest request) {
        ComprarJogadorResponse response = comprarJogadorUseCase.executar(request.clubeId(), request.jogadorId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/jogadores/{id}/vender")
    public ResponseEntity<VendaJogadorResponseDTO> venderJogador(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(venderJogadorUseCase.executar(id));
    }
}
