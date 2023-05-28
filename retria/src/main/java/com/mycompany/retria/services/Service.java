package com.mycompany.retria.services;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.discos.Volume;
import com.github.britooo.looca.api.group.rede.RedeInterface;
import com.mycompany.retria.DAO.AdministradorDAO;
import com.mycompany.retria.DAO.EspecificacaoComponenteDAO;
import com.mycompany.retria.DAO.MaquinaUltrassomDAO;
import com.mycompany.retria.DAO.MaquinaUltrassomEspecificadaDAO;
import com.mycompany.retria.MODEL.*;
import com.mycompany.retria.exception.ValidacaoException;
import com.mycompany.retria.validadores.ValidadorDeComponentes;

import java.util.*;

public class Service {
    private Administrador adm;
    private MaquinaUltrassom maquinaUltrassom;
    private List<EspecificacaoComponente> especificacaoComponente = new ArrayList<>();
    private List<MaquinaUltrassomEspecificada> maquinaUltrassomEspec = new ArrayList<>();
    private List<RedeInterface> redeAtual = new ArrayList<>();

    private Looca looca = new Looca();


    public void scriptDeValidacaoDeBanco(String emailAdm, String senha) {

        AdministradorDAO admDao = new AdministradorDAO();

        MaquinaUltrassomDAO maquinaUltrassomDAO = new MaquinaUltrassomDAO();

        EspecificacaoComponenteDAO especificacaoComponenteDAO = new EspecificacaoComponenteDAO();

        MaquinaUltrassomEspecificadaDAO maquinaUltrassomEspecificadaDAO = new MaquinaUltrassomEspecificadaDAO();

        List<Volume> discos = looca.getGrupoDeDiscos().getVolumes();

        System.out.println("Estou na service!!!!!");

        adm = admDao.setAdministrador(emailAdm, senha);
        maquinaUltrassom = maquinaUltrassomDAO.getMaquinaUltrassom(looca.getProcessador().getId(), adm.getId_administrador(), adm.getFk_empresa(),
                looca.getSistema().getSistemaOperacional());

        System.out.println(maquinaUltrassom);

        if (!maquinaUltrassom.getStatusMaquina().equals("true")) {
            System.out.println("Maquina não autorizada! Contate  o seu administrador!");
            return;
        }

            especificacaoComponente.add(especificacaoComponenteDAO.getComponenteCpu(looca.getProcessador()));
            especificacaoComponente.add(especificacaoComponenteDAO.getComponenteMemoria(looca.getMemoria()));
            especificacaoComponente.add(especificacaoComponenteDAO.getRede(looca.getRede().getGrupoDeInterfaces().getInterfaces()));

            for (Volume disco : discos) {
                System.out.println("VOCÊ TEM " + discos.size() + " discos\n");
                System.out.println("DISCO ATUAL\n");
                System.out.println(disco);

                if (convertBytesToGB(disco.getTotal()) >= 1) {
                    EspecificacaoComponente retorno = especificacaoComponenteDAO.getComponenteDisco(disco);
                    if (retorno != null) {
                        especificacaoComponente.add(retorno);
                    }
                }
            }

            maquinaUltrassomEspec.add(maquinaUltrassomEspecificadaDAO.getMaquiUltassomEspecCPU(100.0,
                    maquinaUltrassom.getIdMaquina(),
                    especificacaoComponente.stream().filter(e -> e.getTipoComponente().equals(TipoComponente.CPU))
                            .findFirst().get().getId_especificacao_componente()
            ));

            maquinaUltrassomEspec.add(maquinaUltrassomEspecificadaDAO.getMaquiUltassomEspecRAM(
                    100.0,
                    maquinaUltrassom.getIdMaquina(),
                    especificacaoComponente.stream().filter(e -> e.getTipoComponente().equals(TipoComponente.RAM))
                            .findFirst().get().getId_especificacao_componente()
            ));

            for (int i = 0; i < especificacaoComponente.size(); i++) {
                EspecificacaoComponente esAtual = especificacaoComponente.get(i);

                System.out.println("Passando essa maquina " + esAtual);
                if (esAtual.getTipoComponente().equals(TipoComponente.DISCO)) {
                    maquinaUltrassomEspec.add(maquinaUltrassomEspecificadaDAO.
                            getMaquiUltassomEspecDISCO(100.0, maquinaUltrassom.
                                    getIdMaquina(), esAtual.getId_especificacao_componente()));
                }
            }

            maquinaUltrassomEspec.add(maquinaUltrassomEspecificadaDAO.getMaquiUltassomEspecRede(
                    maquinaUltrassom.getIdMaquina(),
                    especificacaoComponente.stream().filter(e -> e.getTipoComponente().equals(TipoComponente.REDE))
                            .findFirst().get().getId_especificacao_componente()
            ));

        }



    public void validarMetrica() throws ValidacaoException {
        MaquinaUltrassomDAO maquinaUltrassomDAO = new MaquinaUltrassomDAO();
        if (!maquinaUltrassom.getStatusMaquina().equals("true")) {
            System.out.println("Maquina não autorizada! Contate  o seu administrador!");
            return;
        }

        List<Volume> discos = looca.getGrupoDeDiscos().getVolumes();
        ValidadorDeComponentes validadorDeComponentes = new ValidadorDeComponentes();
        TesteConexaoComFornecedor testeConexaoComFornecedor = new TesteConexaoComFornecedor();

        Integer fkCpuEspec = especificacaoComponente.stream()
                .filter(e -> e.getTipoComponente().equals(TipoComponente.CPU))
                .findFirst().get().getId_especificacao_componente();

        Integer fkRamEspec = especificacaoComponente.stream()
                .filter(e -> e.getTipoComponente().equals(TipoComponente.RAM))
                .findFirst().get().getId_especificacao_componente();

        Integer fkRedeEspec = especificacaoComponente.stream()
                .filter(e -> e.getTipoComponente().equals(TipoComponente.REDE))
                .findFirst().get().getId_especificacao_componente();

        Integer fkCpu = maquinaUltrassomEspec.stream().filter(e -> e.getFk_especificacao_componente()
                .equals(fkCpuEspec)).findFirst().get().getId_especificacao_componente_maquina();

        Integer fkRam = maquinaUltrassomEspec.stream().filter(e -> e.getFk_especificacao_componente()
                .equals(fkRamEspec)).findFirst().get().getId_especificacao_componente_maquina();

        Integer fkRede = maquinaUltrassomEspec.stream().filter(e -> e.getFk_especificacao_componente()
                .equals(fkRedeEspec)).findFirst().get().getId_especificacao_componente_maquina();

        List<EspecificacaoComponente> componentesDisc = especificacaoComponente.stream()
                .filter(e -> e.getTipoComponente().equals(TipoComponente.DISCO)).toList();

        RedeInterface redeAtual = looca.getRede().getGrupoDeInterfaces().getInterfaces().stream().
                filter(r -> r.getBytesRecebidos() > 0 && r.getBytesEnviados() > 0).findFirst().get();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!maquinaUltrassomDAO.getStatusAtual(maquinaUltrassom.getNumeroSerialMaquina()).equals("true")) {
                    System.out.println("Maquina não autorizada! Contate  o seu administrador!");
                } else {

                    try {
                        validadorDeComponentes.validarCpu(looca.getProcessador(), fkCpu);
                    } catch (ValidacaoException e) {
                        System.out.println(e);
                    }

                    try {
                        validadorDeComponentes.validarRam(looca.getMemoria(), fkRam);
                    } catch (ValidacaoException e) {
                        System.out.println(e);
                    }

                    for (int i = 0; i < componentesDisc.size(); i++) {
                        try {
                            System.out.println("tamanho da componentes " + componentesDisc.size());
                            System.out.println("VOLTA " + i);
                            EspecificacaoComponente especAtual = componentesDisc.get(i);

                            Integer fkDiscoEspec = especAtual.getId_especificacao_componente();

                            Integer fkDisco = maquinaUltrassomEspec.stream().filter(e -> e.getFk_especificacao_componente()
                                    .equals(fkDiscoEspec)).findFirst().get().getId_especificacao_componente_maquina();

                            Volume discoAtual = discos.stream().filter(e -> e.getUUID()
                                    .equals(especAtual.getNumero_serial())).findFirst().get();
                            if (convertBytesToGB(discoAtual.getTotal()) >= 1) {
                                validadorDeComponentes.validarDisco(discoAtual, fkDisco);
                            }

                        } catch (ValidacaoException e) {
                            System.out.println(e);
                        }
                    }

                    validadorDeComponentes.validarRede(redeAtual, fkRede);

                    testeConexaoComFornecedor.execLog("54.226.244.94", adm.getNome_administrador(),
                            maquinaUltrassom.getStatusMaquina(),maquinaUltrassom.getIdMaquina());
                }
            }
        }, 0, 10000);

    }

    public void plotarIntroducaoAscii() {
        System.out.println("""
                                
                 _____  _____  ____    ____  ____    ____   ______             _______     ________  _________  _______     _____       _      \s
                |_   _||_   _||_   \\  /   _||_   \\  /   _|.' ____ \\           |_   __ \\   |_   __  ||  _   _  ||_   __ \\   |_   _|     / \\     \s
                  | |    | |    |   \\/   |    |   \\/   |  | (___ \\_|  ______    | |__) |    | |_ \\_||_/ | | \\_|  | |__) |    | |      / _ \\    \s
                  | '    ' |    | |\\  /| |    | |\\  /| |   _.____`.  |______|   |  __ /     |  _| _     | |      |  __ /     | |     / ___ \\   \s
                   \\ \\__/ /    _| |_\\/_| |_  _| |_\\/_| |_ | \\____) |           _| |  \\ \\_  _| |__/ |   _| |_    _| |  \\ \\_  _| |_  _/ /   \\ \\_ \s
                    `.__.'    |_____||_____||_____||_____| \\______.'          |____| |___||________|  |_____|  |____| |___||_____||____| |____|\s
                \n                                                                                                                             \s
                \n           
                """);

    }

    public void plotarIntroducao() {
        System.out.println("""
                ===============================================
                |                Bem-vindo(a)!                |
                ===============================================
                | Você iniciou o aplicativo UMMS da Retria    |
                -----------------------------------------------
                | A seguir insira o seu email de administrador|
                -----------------------------------------------
                | e sua senha para que possamos prosseguir!   |
                ===============================================
                \n
                """);
    }

    public void plotarMenu() {
        System.out.println("""
                ===============================================
                |            Escolha uma das opções           |
                ===============================================
                | 1 - Realizar login                          |
                ===============================================
                | 0 - Sair                                    |
                ===============================================
                \n
                """);
    }

    public void pularLinha() {
        System.out.println("\n");
    }

    public Double convertBytesToGB(long bytes) {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }
    public Double convertBytesToMB(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
}
