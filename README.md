# Psoft-Arenaa

Trabalho da cadeira de **Projeto de Software** (UFCG). API REST em Spring Boot em que professores cadastram turmas, problemas e estratégias de avaliação, criam atividades para as turmas, e estudantes submetem soluções em Java que são avaliadas automaticamente por um LLM.

## Grupo
 
- Cláudio Pinto Lopes Filho
- Pedro Henrique Coelho Torres
- Laerson Alves de Medeiros Neto
- Luiz Marcelo Rabelo Borges Da Costa Santos
- Davi Pereira Ferreira


## Como rodar
 
Requisitos: Java 17 ou superior e Maven. Os testes foram executados com sucesso utilizando Java 25.
 
```bash
git clone https://github.com/Pedroct06/Psoft-Arenaa.git
cd Psoft-Arenaa
export ARENA_LLM_CHAVE_MESTRA=$(openssl rand -base64 32)
mvn spring-boot:run
```

## Cobertura de testes

A cobertura de testes é medida utilizando o JaCoCo.

Para executar os testes e gerar o relatório, utilize `mvn -Dnet.bytebuddy.experimental=true clean verify`.

Resultados da cobertura:

| Métrica | Cobertura |
|---|---:|
| Linhas | 94,84% |
| Instruções | 94,43% |
| Branches | 84,00% |

O relatório HTML pode ser consultado em `target/site/jacoco/index.html`.
