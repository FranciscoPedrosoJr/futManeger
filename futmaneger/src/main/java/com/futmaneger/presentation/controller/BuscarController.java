package com.futmaneger.presentation.controller;

import com.futmaneger.application.dto.*;
import com.futmaneger.application.usecase.campeonato.BuscarCampeoesUseCase;
import com.futmaneger.application.usecase.campeonato.BuscarCampeonatosUseCase;
import com.futmaneger.application.usecase.campeonato.BuscarPartidasMataMataUseCase;
import com.futmaneger.application.usecase.campeonato.BuscarTabelaCampeonatoUseCase;
import com.futmaneger.application.usecase.clube.BuscarClubesUseCase;
import com.futmaneger.application.usecase.clube.ListarJogadoresDoClubeUseCase;
import com.futmaneger.application.usecase.jogador.BuscarJogadoresUseCase;
import com.futmaneger.application.usecase.rodadas.BuscarRodadasNaoFinalizadasUseCase;
import com.futmaneger.application.usecase.rodadas.BuscarRodadasPorCampeonatoUseCase;
import com.futmaneger.application.usecase.tecnico.BuscarTecnicoUseCase;
import com.futmaneger.infrastructure.persistence.entity.ClubeEntity;
import com.futmaneger.infrastructure.persistence.entity.TecnicoEntity;
import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/buscar")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BuscarController {
    private final BuscarClubesUseCase buscarClubesUseCase;
    private final BuscarTecnicoUseCase buscarTecnicoUseCase;
    private final BuscarJogadoresUseCase buscarJogadoresUseCase;
    private final BuscarPartidasMataMataUseCase buscarPartidasMataMataUseCase;
    private final BuscarTabelaCampeonatoUseCase buscarTabelaCampeonatoUseCase;
    private final BuscarCampeoesUseCase buscarCampeoesUseCase;
    private final ListarJogadoresDoClubeUseCase listarJogadoresDoClubeUseCase;
    private final BuscarCampeonatosUseCase buscarCampeonatosUseCase;
    private final BuscarRodadasNaoFinalizadasUseCase buscarRodadasNaoFinalizadasUseCase;
    private final BuscarRodadasPorCampeonatoUseCase  buscarRodadasPorCampeonatoUseCase;

    @GetMapping("/clubes")
    public ResponseEntity<List<ClubeEntity>> listarClubes() {
        return ResponseEntity.ok(buscarClubesUseCase.buscarTodos());
    }

    @GetMapping("/tecnicos")
    public ResponseEntity<List<TecnicoEntity>> listarTecnicos() {
        return ResponseEntity.ok(buscarTecnicoUseCase.buscarTodos());
    }

    @PostMapping("/jogadores")
    public ResponseEntity<List<JogadorResponseDTO>> buscar(@RequestBody JogadorFiltroDTO filtro) {
        List<JogadorResponseDTO> jogadores = buscarJogadoresUseCase.buscar(filtro);
        return ResponseEntity.ok(jogadores);
    }

    @GetMapping("edicao/{id}/partidas-mata-mata")
    public ResponseEntity<List<PartidaMataMataResponseDTO>> listarPartidasMataMata(@PathVariable Long id) {
        List<PartidaMataMataResponseDTO> response = buscarPartidasMataMataUseCase.buscarPorEdicao(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar-tabela/edicao/{idEdicao}")
    public ResponseEntity<List<TabelaCampeonatoResponseDTO>> buscar(@PathVariable Long idEdicao) {
        return ResponseEntity.ok(buscarTabelaCampeonatoUseCase.executar(idEdicao));
    }

    @GetMapping("/campeoes")
    public ResponseEntity<List<CampeaoResponseDTO>> listarCampeoes(
            @RequestParam(required = false) Long edicaoId
    ) {
        return ResponseEntity.ok(buscarCampeoesUseCase.buscarCampeoes(edicaoId));
    }

    @GetMapping("clube/{clubeId}/jogadores")
    public ResponseEntity<JogadoresDoClubeResponseDTO> listar(@PathVariable Long clubeId) {
        return ResponseEntity.ok(listarJogadoresDoClubeUseCase.executar(clubeId));
    }

    @GetMapping("/campeonatos")
    public ResponseEntity<List<CampeonatoBuscaResponseDTO>> buscarTodos() {
        var campeonatos = buscarCampeonatosUseCase.executar();
        return ResponseEntity.ok(campeonatos);
    }

    @GetMapping("/rodadas-nao-finalizadas")
    public ResponseEntity<List<RodadaResponseDTO>> buscarRodadasNaoFinalizadas() {
        return ResponseEntity.ok(buscarRodadasNaoFinalizadasUseCase.executar());
    }

    @GetMapping("/edicoes/{edicaoId}/rodadas")
    public ResponseEntity<List<RodadaResponseDTO>> buscarRodadasPorEdicao(
            @PathVariable Long edicaoId) {

        return ResponseEntity.ok(
                buscarRodadasPorCampeonatoUseCase.executar(edicaoId)
        );
    }
}
