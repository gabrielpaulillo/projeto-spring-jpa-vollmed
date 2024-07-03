package med.voll.api.domain.consulta;

import jdk.jfr.Experimental;
import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Representa um serviço da aplicação (agendamento de consultas)
@Service
public class AgendaDeConsulta {
//    Essa classe detém as regras de negócio
//    O objetivo é salvar o agendamento no banco de dados: recebemos a requisição com os dados de agendamento e precisamos salvá-los na tabela de consultas
//    Por isso, precisamos acessar o banco de dados e a tabela de consultas nesta classe. Assim, declararemos um atributo ConsultaRepository

    @Autowired // para injetar o repositório em nossa classe
    private ConsultaRepository consultaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    public void agendar(DadosAgendamentoConsulta dados) {
        if (!pacienteRepository.existsById(dados.idPaciente())) {
            throw new ValidacaoException("Id do paciente informado não existe!");
        }

        if (dados.idMedico() != null && !medicoRepository.existsById(dados.idMedico())) {
            throw new ValidacaoException("Id do médico informado não existe!");
        }

        var medico = escolherMedico(dados);
        var paciente = pacienteRepository.findById(dados.idPaciente()).get(); // Na requisição só vem o ID, mas precisamos carregar o objeto inteiro. Assim, usamos o Repository para carregar pelo ID do banco de dados.
//        Aparecerá um erro de compilação porque o método findById() não devolve a entidade, mas um Optional. Assim, no fim da linha, antes do ponto e vírgula, precisamos escrever .get() ao lado de findById(). Isso faz com que ele pegue a entidade carregada.
        var consulta = new Consulta(null, medico, paciente, dados.data(), null);
        consultaRepository.save(consulta);
    }

    private Medico escolherMedico(DadosAgendamentoConsulta dados) {
        if (dados.idMedico() != null) {
            return medicoRepository.getReferenceById(dados.idMedico());
        }

        if (dados.especialidade() == null) {
            throw new ValidacaoException("Especialidade é obrigatória quando médico não for escolhido!");
        }

        return medicoRepository.escolherMedicoAleatorioLivreNaData(dados.especialidade(), dados.data());
    }

    public void cancelar(DadosCancelamentoConsulta dados) {
        if (!consultaRepository.existsById(dados.idConsulta())) {
            throw new ValidacaoException("Id da consulta informado não existe!");
        }

        var consulta = consultaRepository.getReferenceById(dados.idConsulta());
        consulta.cancelar(dados.motivo());
    }
}
