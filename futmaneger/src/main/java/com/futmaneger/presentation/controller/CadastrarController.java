package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.CampeonatoResponseDTO;
import com.futmaneger.application.dto.CriarCampeonatoRequestDTO;
import com.futmaneger.application.dto.GerarRodadasRequestDTO;
import com.futmaneger.application.dto.GerarRodadasResponseDTO;
import com.futmaneger.application.dto.JogadorLoteRequestDTO;
import com.futmaneger.application.dto.JogadorResponseDTO;
import com.futmaneger.application.usecase.campeonato.CriarCampeonatoUseCase;
import com.futmaneger.application.usecase.clube.CadastrarClubeUseCase;
import com.futmaneger.application.usecase.jogador.CadastrarJogadoresUseCase;
import com.futmaneger.application.usecase.rodadas.GerarRodadasUseCase;
import com.futmaneger.presentation.request.CadastrarClubeRequest;
import com.futmaneger.presentation.response.ClubeResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cadastrar")
public class CadastrarController {
    private final CadastrarClubeUseCase cadastrarClubeUseCase;

    private final CadastrarJogadoresUseCase cadastrarJogadoresUseCase;

    private final GerarRodadasUseCase gerarRodadasUseCase;

    private final CriarCampeonatoUseCase criarCampeonatoUseCase;

    public CadastrarController(CadastrarClubeUseCase cadastrarClubeUseCase, CadastrarJogadoresUseCase cadastrarJogadoresUseCase,
                               GerarRodadasUseCase gerarRodadasUseCase, CriarCampeonatoUseCase criarCampeonatoUseCase) {
        this.cadastrarClubeUseCase = cadastrarClubeUseCase;
        this.cadastrarJogadoresUseCase = cadastrarJogadoresUseCase;
        this.gerarRodadasUseCase = gerarRodadasUseCase;
        this.criarCampeonatoUseCase = criarCampeonatoUseCase;
    }

    @PostMapping("/clubes")
    public ResponseEntity<ClubeResponse> cadastrar(@RequestBody CadastrarClubeRequest request) {
        var clube = cadastrarClubeUseCase.executar(request.nome(), request.estado(), request.pais());
        return ResponseEntity.ok(new ClubeResponse(clube));
    }

    @PostMapping("/jogadores")
    public ResponseEntity<List<JogadorResponseDTO>> cadastrarJogadores(@RequestBody JogadorLoteRequestDTO request) {
        var response = cadastrarJogadoresUseCase.cadastrarLote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/campeonatos/{id}/rodadas")
    public ResponseEntity<GerarRodadasResponseDTO> gerarRodadas(@PathVariable Long id) {
        var response = gerarRodadasUseCase.executar(new GerarRodadasRequestDTO(id));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/campeonatos")
    public ResponseEntity<CampeonatoResponseDTO> criar(@RequestBody CriarCampeonatoRequestDTO request) {
        CampeonatoResponseDTO response = criarCampeonatoUseCase.executar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
