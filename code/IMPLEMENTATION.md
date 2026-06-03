# Tango Solver — Documentação Técnica Completa

> TP2 — Fundamentos de Projeto e Análise de Algoritmos · PUC Minas 2026/1  
> Projeto: resolução do quebra-cabeça Tango com Força Bruta e Backtracking em Java

---

## Sumário

1. [O Problema — Quebra-Cabeça Tango](#1-o-problema--quebra-cabeça-tango)
2. [Arquitetura do Projeto](#2-arquitetura-do-projeto)
3. [Modelagem do Domínio](#3-modelagem-do-domínio)
4. [Formato dos Arquivos de Entrada](#4-formato-dos-arquivos-de-entrada)
5. [Tabuleiros Disponíveis](#5-tabuleiros-disponíveis)
6. [Regras de Validação](#6-regras-de-validação)
7. [Framework CSP Genérico](#7-framework-csp-genérico)
8. [Algoritmo de Força Bruta](#8-algoritmo-de-força-bruta)
9. [Algoritmo de Backtracking](#9-algoritmo-de-backtracking)
10. [Adaptador e Seleção de Variável](#10-adaptador-e-seleção-de-variável)
11. [Métricas e Cálculos](#11-métricas-e-cálculos)
12. [Resultados de Execução](#12-resultados-de-execução)
13. [Análise Comparativa](#13-análise-comparativa)

---

## 1. O Problema — Quebra-Cabeça Tango

O Tango é um quebra-cabeça de dedução lógica jogado em uma grade N×N. Cada célula da grade precisa ser preenchida com um de dois símbolos possíveis: **Sol (☀)** ou **Lua (☽)**. O tabuleiro começa parcialmente preenchido — algumas células já têm um símbolo definido como dica inicial — e o objetivo é descobrir, por raciocínio lógico, o único preenchimento possível para todas as células restantes.

O que torna o Tango interessante como problema computacional é que ele pertence à categoria de **problemas de satisfação de restrições (CSP — Constraint Satisfaction Problem)**. A solução não é encontrada por cálculo matemático direto, mas por um processo de tentativa e eliminação sistemática: o algoritmo atribui símbolos às células e verifica continuamente se as regras do jogo estão sendo respeitadas. Quando uma violação é detectada, o algoritmo precisa "voltar atrás" e tentar uma combinação diferente.

Todo tabuleiro Tango bem formulado possui **exatamente uma solução válida** — propriedade garantida pelo enunciado. Isso significa que não há ambiguidade: ou o algoritmo encontra aquela solução específica, ou falhou.

### 1.1 As 5 Regras do Domínio

Existem cinco regras que definem o que é uma solução válida. Todas devem ser satisfeitas **simultaneamente** — satisfazer quatro e violar uma já invalida o tabuleiro inteiro.

**Regra 1 — Preenchimento Completo:** toda célula deve conter obrigatoriamente um Sol ou uma Lua. Nenhuma pode permanecer vazia no estado final.

**Regra 2 — Limite de Adjacência:** não é permitido que três ou mais símbolos idênticos apareçam em sequência direta, seja na horizontal ou na vertical. O máximo são dois iguais seguidos. Exemplo: `☀ ☀ ☽` é válido, mas `☀ ☀ ☀` é inválido.

**Regra 3 — Equilíbrio:** cada linha individualmente e cada coluna individualmente deve conter exatamente metade Sóis e metade Luas. Em uma grade 6×6, isso é 3 de cada por linha e 3 de cada por coluna.

**Regra 4 — Restrição `=`:** células adjacentes ligadas por `=` devem ter o mesmo símbolo. Se uma é Sol, a outra também deve ser Sol.

**Regra 5 — Restrição `×`:** células adjacentes ligadas por `×` devem ter símbolos opostos. Se uma é Sol, a outra deve ser Lua.

---

## 2. Arquitetura do Projeto

O projeto foi estruturado em **duas camadas completamente independentes** que se comunicam exclusivamente por meio de interfaces Java. Essa separação tem uma razão fundamental: os algoritmos de busca (Força Bruta e Backtracking) são técnicas genéricas que podem ser aplicadas a qualquer problema de satisfação de restrições, não apenas ao Tango. Ao separar o "motor de busca" da "lógica do Tango", o código deixa claro o que é algoritmo e o que é domínio do problema.

A camada `csp/` contém os engines de busca e as interfaces abstratas que eles precisam para funcionar. Ela não importa nada relacionado ao Tango — não conhece Sol, Lua, tabuleiro, linha, coluna ou qualquer regra do jogo. Para ela, existe apenas um "estado genérico" que pode ser atribuído, desfeito e verificado.

A camada `tango/` contém tudo que é específico do quebra-cabeça: a grade, as células, as regras de validação, a leitura dos arquivos e a impressão do resultado. Ela implementa os contratos definidos pela camada CSP, traduzindo o domínio do Tango para a linguagem que os engines entendem.

```
src/main/java/tp2/fpaa/
│
├── csp/                        ← CAMADA 1: framework genérico de busca
│   ├── contract/
│   │   ├── State.java
│   │   ├── ConstraintChecker.java
│   │   ├── VariableSelector.java
│   │   └── ValueIterator.java
│   ├── engine/
│   │   ├── BacktrackingEngine.java
│   │   └── BruteForceEngine.java
│   └── result/
│       └── SolveResult.java
│
├── tango/                      ← CAMADA 2: domínio específico do Tango
│   ├── domain/
│   │   ├── Symbol.java
│   │   ├── ConstraintType.java
│   │   └── Constraint.java
│   ├── board/
│   │   ├── Cell.java
│   │   ├── Board.java
│   │   └── BoardFactory.java
│   ├── validation/
│   │   ├── Rule.java
│   │   ├── ConsecutiveRule.java
│   │   ├── BalanceRule.java
│   │   ├── EqualRule.java
│   │   ├── OppositionRule.java
│   │   └── TangoConstraintChecker.java
│   ├── heuristic/
│   │   ├── TangoVariableSelector.java
│   │   └── TangoValueIterator.java
│   ├── adapter/
│   │   └── TangoBoardAdapter.java
│   └── io/
│       ├── BoardParser.java
│       ├── BoardPrinter.java
│       ├── ResultPrinter.java
│       └── RunResult.java
│
└── Main.java
```

### 2.1 Como as Duas Camadas se Comunicam

O ponto de contato entre as camadas acontece exclusivamente no `Main.java`. É lá que as implementações concretas do Tango são instanciadas e depois injetadas nos engines. O trecho abaixo mostra essa montagem:

```java
TangoConstraintChecker checker = new TangoConstraintChecker(List.of(
    new ConsecutiveRule(), new BalanceRule(),
    new EqualRule(),       new OppositionRule()
));
TangoVariableSelector selector = new TangoVariableSelector();
TangoValueIterator    iterator = new TangoValueIterator();

new BacktrackingEngine<>(checker, selector, iterator).solve(adapter);
```

Cada linha dessa montagem tem um papel específico. O `TangoConstraintChecker` é criado recebendo uma lista com as quatro regras do jogo — ele será chamado pelo engine toda vez que precisar saber se o estado atual é válido. O `TangoVariableSelector` é o objeto responsável por decidir qual célula do tabuleiro o engine vai preencher a seguir. O `TangoValueIterator` é o objeto que informa quais são os valores possíveis para preencher uma célula — no Tango, sempre Sol ou Lua.

O `BacktrackingEngine` recebe os três objetos via construtor. A partir desse momento, o engine só enxerga as interfaces genéricas: `ConstraintChecker`, `VariableSelector` e `ValueIterator`. Ele não sabe que por trás do `checker` existem quatro regras do Tango, nem que por trás do `selector` existe uma lógica que percorre células de um tabuleiro. Para o engine, são abstrações genéricas — e esse isolamento é o objetivo da arquitetura.

O `adapter` passado para `.solve()` é um `TangoBoardAdapter`, que envolve o `Board` lido do arquivo. Ele implementa a interface `State` que o engine espera, traduzindo operações genéricas (atribuir variável X com valor Y) para operações concretas do Tango (preencher a célula no índice X com o símbolo Y).

---

## 3. Modelagem do Domínio

### 3.1 Symbol — os Dois Valores Possíveis

```java
public enum Symbol {
    SUN, MOON;

    public Symbol opposite() {
        return this == SUN ? MOON : SUN;
    }
}
```

`Symbol` é um `enum` Java com exatamente dois valores nomeados: `SUN` (Sol) e `MOON` (Lua). A escolha por `enum` em vez de um inteiro simples (como `0` e `1`) ou uma `String` (como `"sol"` e `"lua"`) tem uma razão de segurança: com `enum`, é impossível criar um símbolo inválido. Se o código tentasse usar um terceiro valor qualquer, o compilador recusaria. Com inteiros, seria possível acidentalmente usar `2` ou `-1` sem nenhum erro de compilação, criando estados incorretos difíceis de depurar.

O método `opposite()` retorna o símbolo contrário ao atual. A expressão `this == SUN ? MOON : SUN` funciona assim: `this` se refere ao objeto `Symbol` sobre o qual o método está sendo chamado. Se esse objeto é `SUN`, a expressão retorna `MOON`. Se é qualquer outra coisa (que neste enum só pode ser `MOON`), retorna `SUN`. Esse método não é chamado diretamente pelo engine durante a busca, mas existe como utilitário para qualquer parte do código que precise do oposto de um símbolo sem precisar escrever condicionais.

Vale notar que os engines trabalham internamente com inteiros `0` e `1` como candidatos de valor — não com objetos `Symbol`. A conversão acontece no momento da atribuição, com `Symbol.values()[value]`: `Symbol.values()` retorna o array `[SUN, MOON]` (na ordem em que foram declarados), e indexar com `0` retorna `SUN`, indexar com `1` retorna `MOON`. Essa convenção é mantida consistentemente em todo o projeto.

### 3.2 ConstraintType — os Dois Tipos de Aresta

```java
public enum ConstraintType {
    EQUAL, OPPOSITE
}
```

`ConstraintType` funciona pelo mesmo princípio do `Symbol`: um enum com dois valores possíveis, garantindo que não existe um terceiro tipo de restrição que não seja `EQUAL` (igualdade, sinal `=`) ou `OPPOSITE` (oposição, sinal `×`). Quando o `BoardParser` lê uma linha do arquivo de entrada e encontra `=`, ele cria uma `Constraint` com tipo `EQUAL`. Quando encontra `x`, cria com tipo `OPPOSITE`. Nas regras de validação, o código compara o tipo da constraint com esses valores para saber qual verificação fazer.

### 3.3 Constraint — a Aresta Entre Duas Células

```java
public final class Constraint {
    private final int firstIndex;
    private final int secondIndex;
    private final ConstraintType type;
}
```

Uma `Constraint` representa uma aresta entre duas células específicas do tabuleiro e define qual relação deve existir entre elas. Os três campos são todos `final`, o que em Java significa que não podem ser alterados após a criação do objeto — a constraint é completamente imutável.

Os campos `firstIndex` e `secondIndex` armazenam as posições das duas células envolvidas, mas não como coordenadas `(linha, coluna)`. Em vez disso, usam um único inteiro chamado **índice linear**, calculado pela fórmula `linha × tamanho_do_tabuleiro + coluna`. Por exemplo, num tabuleiro 6×6, a célula na linha 2, coluna 3 tem índice `2 × 6 + 3 = 15`. Essa escolha existe porque o tabuleiro é internamente um array unidimensional, e o índice linear permite acessar qualquer célula diretamente sem conversão extra.

O campo `type` armazena se a relação é de igualdade ou oposição. Ao validar uma constraint, o código lê os símbolos das células nos índices `firstIndex` e `secondIndex` e, dependendo do `type`, verifica se são iguais ou opostos.

A imutabilidade das constraints tem uma consequência importante: quando `BoardFactory` faz uma cópia profunda do tabuleiro para iniciar uma nova execução de busca, ele não precisa copiar a lista de constraints. Como constraints nunca mudam, a cópia do tabuleiro pode compartilhar a mesma lista de constraints do original sem risco de inconsistência.

### 3.4 Cell — a Unidade do Tabuleiro

```java
public final class Cell {
    private Symbol symbol;
    private final boolean fixed;
}
```

Cada `Cell` representa uma célula da grade e armazena duas informações: o símbolo atual nela (`symbol`) e se ela é uma dica fixa (`fixed`).

O campo `symbol` é o único campo não-`final` em toda a modelagem do domínio — e isso é proposital. Durante a busca, os engines precisam atribuir um símbolo à célula e depois, se necessário, desfazer essa atribuição. Essa mutabilidade controlada é o mecanismo que permite a busca funcionar: em vez de criar um objeto novo a cada tentativa (o que seria caro em memória), o engine modifica o símbolo da célula existente.

Quando `symbol` é `null`, significa que a célula está vazia — ainda não foi preenchida pela busca. O método `isEmpty()` simplesmente verifica se `symbol == null`, retornando `true` quando a célula está vazia. Esse método é usado pelo `TangoVariableSelector` para encontrar a próxima célula a preencher, e pelo `isComplete()` do `TangoBoardAdapter` para verificar se o tabuleiro está totalmente preenchido.

O campo `fixed` indica se a célula é uma dica do puzzle, definida no arquivo de entrada. Células fixas têm `fixed = true` e nunca devem ser alteradas. O método `setSymbol()` da `Cell` verifica isso:

```java
public void setSymbol(Symbol symbol) {
    if (fixed) {
        throw new IllegalStateException("Não é possível modificar uma célula fixa");
    }
    this.symbol = symbol;
}
```

A primeira coisa que o método faz é checar `if (fixed)`. Se a célula é fixa, lança uma exceção imediatamente, interrompendo a execução com uma mensagem de erro clara. Essa verificação existe como salvaguarda: na prática, o `TangoVariableSelector` nunca seleciona células fixas (porque elas nunca estão vazias), mas a proteção na `Cell` garante que mesmo que algum bug externo tentasse modificar uma célula fixa, o erro seria detectado imediatamente em vez de silenciosamente corromper o estado do tabuleiro.

Se a célula não é fixa, `this.symbol = symbol` simplesmente atualiza o campo. Quando `symbol` passado é `null`, isso equivale a "esvaziar" a célula — o que é exatamente o que o engine faz ao desfazer uma atribuição (backtrack).

### 3.5 Board — a Grade Completa

```java
public final class Board {
    private final int size;
    private final Cell[] cells;
    private final List<Constraint> constraints;

    public Cell getCell(int row, int col) {
        return cells[row * size + col];
    }

    public Cell getCellAt(int index) {
        return cells[index];
    }

    public int cellCount() {
        return size * size;
    }
}
```

O `Board` representa o tabuleiro completo: suas células e suas restrições de aresta. O campo `size` guarda o lado da grade (por exemplo, `6` para um tabuleiro 6×6). O campo `cells` é um array unidimensional de `Cell` com `size × size` elementos. O campo `constraints` é a lista de arestas de restrição entre células adjacentes.

A decisão de armazenar a grade como array unidimensional em vez de uma matriz bidimensional (`Cell[][]`) tem uma razão direta: os engines identificam células por um único inteiro (o índice linear), não por um par `(linha, coluna)`. Com array unidimensional, o acesso `cells[index]` é uma operação de tempo constante sem cálculo extra. Se fosse uma matriz, seria necessário converter `index` de volta para `(linha, coluna)` a cada acesso.

O método `getCell(row, col)` converte coordenadas bidimensionais para o índice linear com `row * size + col` e retorna a célula correspondente. Ele é usado pelas regras de validação, que percorrem linhas e colunas com dois contadores separados para linha e coluna. O método `getCellAt(index)` acessa diretamente pelo índice linear, sem conversão — usado pelos engines e pelo adaptador. O método `cellCount()` retorna `size * size`, que é o número total de células no tabuleiro.

A lista de constraints é criada com `List.copyOf()` no construtor do `Board`. Isso cria uma lista imutável — após a criação do tabuleiro, nenhum código externo pode adicionar ou remover constraints. Isso é uma proteção defensiva: as constraints fazem parte da definição do puzzle e não devem mudar durante a resolução.

### 3.6 BoardFactory — a Cópia Profunda

```java
public static Board deepCopy(Board original) {
    int size = original.getSize();
    Cell[] copiedCells = new Cell[size * size];
    for (int i = 0; i < copiedCells.length; i++) {
        Cell source = original.getCellAt(i);
        copiedCells[i] = new Cell(source.getSymbol(), source.isFixed());
    }
    return new Board(size, copiedCells, original.getConstraints());
}
```

Este método cria uma cópia independente de um tabuleiro. Entender por que ele é necessário exige entender como Java funciona com objetos: em Java, variáveis guardam referências para objetos, não os objetos em si. Se o engine simplesmente fizesse `Board copia = original`, a `copia` e o `original` apontariam para o mesmo array de células — modificar uma célula pelo `original` alteraria o que a `copia` enxerga, e vice-versa. Isso seria desastroso durante a busca.

O método resolve isso criando novos objetos `Cell` independentes. Linha a linha: primeiro calcula `size` e cria um novo array `copiedCells` vazio com o mesmo tamanho. Depois, para cada índice `i` de `0` até `size × size - 1`, pega a célula original com `original.getCellAt(i)`, lê seu símbolo com `source.getSymbol()` e seu flag fixo com `source.isFixed()`, e cria uma nova `Cell` com esses mesmos valores. Esse novo objeto `Cell` existe de forma completamente independente do original — modificar o símbolo da cópia não afeta o original.

Por fim, cria e retorna um `new Board` com o `size` original, o novo array de células copiadas, e `original.getConstraints()` — a **mesma** lista de constraints do original, sem copiar. Isso é seguro precisamente porque as constraints são imutáveis: nenhum código pode alterar a lista ou os objetos dentro dela, então compartilhá-la entre o original e a cópia não causa nenhum problema.

---

## 4. Formato dos Arquivos de Entrada

Os tabuleiros são definidos em arquivos `.txt`. O `BoardParser` lê esses arquivos e constrói um objeto `Board`. A estrutura do arquivo é:

```
<N>
<linha 0 com N tokens separados por espaço>
<linha 1>
...
<linha N-1>
<constraint 0>
<constraint 1>
...
```

A primeira linha contém apenas o número `N`, que define o lado da grade. As próximas `N` linhas descrevem as células: `S` indica Sol fixo, `M` indica Lua fixa, e `.` indica célula vazia. As linhas restantes descrevem as constraints no formato `= <linha1> <coluna1> <linha2> <coluna2>` para igualdade ou `x <linha1> <coluna1> <linha2> <coluna2>` para oposição. As coordenadas são baseadas em zero.

O `BoardParser.parse()` lê todas as linhas do arquivo de uma vez em uma lista de strings, depois processa essa lista em três etapas: lê `N` da posição 0, lê as células das posições 1 até N, e lê as constraints das posições N+1 em diante. Para cada linha de célula, divide por espaços em branco e converte cada token: `S` vira `new Cell(Symbol.SUN, true)`, `M` vira `new Cell(Symbol.MOON, true)`, e qualquer outra coisa (`.`) vira `new Cell(null, false)`. Para cada linha de constraint, o primeiro token define o tipo e os quatro números seguintes definem as duas células — cada par `(linha, coluna)` é convertido para índice linear com `linha × N + coluna`.

---

## 5. Tabuleiros Disponíveis

O projeto inclui seis tabuleiros para teste, variando em tamanho e dificuldade. A dificuldade não é apenas uma questão de tamanho: dois tabuleiros do mesmo tamanho podem ter dificuldades muito diferentes dependendo de quantas células fixas existem e de quantas e quais constraints estão definidas. Quanto menos células fixas e constraints, maior o espaço de busca efetivo e mais difícil para os algoritmos convergirem rapidamente.

### 5.1 board_4x4.txt — Grade 4×4

O menor tabuleiro. Serve principalmente como caso de teste onde ambos os algoritmos executam em tempo aceitável, permitindo comparação direta.

**Grade inicial:**
```
S . . .
. . . .
. . S .
. . . S
```

**Células fixas:** 3 Sóis nas posições (0,0), (2,2) e (3,3). **Células vazias:** 13, gerando espaço de busca de 2^13 = 8.192 combinações. **Constraints:** 8 arestas: igualdade entre (0,0)↔(0,1), (0,2)↔(0,3), (1,0)↔(1,1), (1,2)↔(1,3), (2,1)↔(2,2); oposição entre (0,1)↔(0,2), (0,0)↔(1,0), (1,1)↔(1,2).

**Solução encontrada:**
```
☀ = ☀ × ☽ = ☽
☽ = ☽ × ☀ = ☀
☽   ☀ = ☀   ☽
☀   ☽   ☽   ☀
```

### 5.2 board_6x6_easy.txt — Grade 6×6 (Fácil)

**Grade inicial:**
```
. . . . S .
. S . . . .
. . M . . S
. . S . . .
. . . M . .
. . . . . .
```

**Células fixas:** 6 — Sol em (0,4), (1,1), (2,5), (3,2); Lua em (2,2), (4,3). **Células vazias:** 30, espaço de 2^30 ≈ 1,07 bilhão. **Constraints:** 10 arestas. A classificação "fácil" vem de 10 constraints num tabuleiro 6×6, o que gera mais oportunidades de poda precoce no Backtracking.

**Solução encontrada:**
```
☀ ☀ ☽ ☽ ☀ ☽
☽ ☀ ☀ ☽ ☀ ☽
☀ ☽ ☽ ☀ ☽ ☀
☀ ☽ ☀ ☀ ☽ ☽
☽ ☀ ☽ ☽ ☀ ☀
☽ ☽ ☀ ☀ ☽ ☀
```

### 5.3 board_6x6_medium.txt — Grade 6×6 (Médio)

**Grade inicial:**
```
. S . . M .
S . . . S .
. . . . . .
. . . S . .
. . . M . .
. . . . . .
```

**Células fixas:** 6 — Sol em (0,1), (1,0), (1,4), (3,3); Lua em (0,4), (4,3). **Células vazias:** 30, espaço de 2^30 ≈ 1,07 bilhão. **Constraints:** 7 arestas — menos que o fácil, o que dá menos oportunidades de poda ao Backtracking e força a Força Bruta a explorar uma fração maior do espaço.

**Solução encontrada:**
```
☀ ☀ ☽ ☀ ☽ ☽
☀ ☽ ☽ ☀ ☀ ☽
☽ ☀ ☀ ☽ ☽ ☀
☀ ☽ ☽ ☀ ☀ ☽
☽ ☀ ☀ ☽ ☽ ☀
☽ ☽ ☀ ☽ ☀ ☀
```

### 5.4 board_6x6_hard.txt — Grade 6×6 (Difícil)

**Grade inicial:**
```
. S . . . .
. . . . . .
S . . . . .
. . . . . .
. S . S . .
. . . . . .
```

**Células fixas:** apenas 4 — Sol em (0,1), (2,0), (4,1), (4,3). **Células vazias:** 32, espaço de 2^32 ≈ 4,29 bilhões. **Constraints:** 7 arestas — igualdade entre (0,1)↔(1,1), (1,0)↔(2,0), (3,4)↔(4,4), (4,2)↔(5,2); oposição entre (0,3)↔(1,3), (3,2)↔(4,2), (4,1)↔(5,1). A menor quantidade de dicas fixas (4 células) é o que torna este tabuleiro o mais difícil dos 6×6: o espaço efetivo de busca é maior, e a Força Bruta precisou percorrer 66% dele antes de encontrar a solução — o que levou 121 segundos.

**Solução encontrada:**
```
☽ ☀ ☀ ☽ ☀ ☽
☀ ☀ ☽ ☀ ☽ ☽
☀ ☽ ☀ ☽ ☽ ☀
☽ ☽ ☀ ☽ ☀ ☀
☽ ☀ ☽ ☀ ☀ ☽
☀ ☽ ☽ ☀ ☽ ☀
```

### 5.5 board_8x8.txt — Grade 8×8

**Grade inicial:**
```
. . S . . . . .
. . . M . . . .
. . M . . . . M
M . . S . . . .
. . M M . . . .
. S . M . S M .
. . S . . . S .
. . . . . . . M
```

**Células fixas:** 15 — mistura de Sóis e Luas. **Células vazias:** 49, espaço de 2^49 ≈ 562 trilhões — completamente inviável para a Força Bruta. **Constraints:** 6 arestas — oposição entre (0,2)↔(1,2), (2,3)↔(2,4), (2,0)↔(3,0), (4,2)↔(5,2), (4,3)↔(4,4); igualdade entre (5,1)↔(5,2). O código detecta automaticamente tabuleiros maiores que 6×6 e pula a Força Bruta, exibindo mensagem de aviso.

**Solução encontrada:**
```
☀ ☽ ☀ ☀ ☽ ☀ ☽ ☽
☽ ☀ ☽ ☽ ☀ ☀ ☽ ☀
☀ ☀ ☽ ☽ ☀ ☽ ☀ ☽
☽ ☽ ☀ ☀ ☽ ☀ ☽ ☀
☀ ☀ ☽ ☽ ☀ ☽ ☀ ☽
☽ ☀ ☀ ☽ ☽ ☀ ☽ ☀
☽ ☽ ☀ ☀ ☽ ☽ ☀ ☀
☀ ☽ ☽ ☀ ☀ ☽ ☀ ☽
```

### 5.6 board_16x16.txt — Grade 16×16

O maior tabuleiro. **Células fixas:** 32, distribuídas com simetria diagonal. **Células vazias:** 224, espaço de 2^224 — um número maior que o número de átomos no universo observável. **Constraints:** 21 arestas (combinação de `=` e `×` horizontais e verticais). Apenas o Backtracking é executado, e encontra a solução em 335 milissegundos com 99,94% das tentativas resultando em poda.

---

## 6. Regras de Validação

Todas as quatro regras implementam a interface `Rule`, que define um único método `check(Board board)` retornando `true` se a regra está satisfeita no estado atual do tabuleiro, ou `false` se está violada. O estado atual pode ser parcialmente preenchido durante o Backtracking — células vazias são representadas por `null` e cada regra trata isso de forma específica para não gerar falsos negativos.

O `TangoConstraintChecker` agrega todas as regras e é o único objeto que os engines conhecem como verificador:

```java
public boolean isValid(TangoBoardAdapter adapter) {
    Board board = adapter.getBoard();
    return rules.stream().allMatch(r -> r.check(board));
}
```

O método `isValid()` recebe o adaptador (que envolve o `Board`), extrai o `Board` com `adapter.getBoard()`, e depois chama `rules.stream().allMatch(r -> r.check(board))`. O `allMatch` percorre cada regra da lista e chama `check(board)` nela. Se qualquer regra retornar `false`, o `allMatch` para imediatamente e retorna `false` sem executar as regras seguintes. Isso é chamado de curto-circuito: assim que uma regra falha, não faz sentido checar as demais. Somente se todas as regras retornarem `true` é que `allMatch` retorna `true`. A ordem das regras na lista importa ligeiramente para eficiência: regras que detectam violações mais frequentemente ou são mais baratas de executar devem vir primeiro. O projeto usa: `ConsecutiveRule`, `BalanceRule`, `EqualRule`, `OppositionRule`.

---

### 6.1 ConsecutiveRule — Regra do Limite de Adjacência

Esta regra verifica que nenhuma linha e nenhuma coluna possui três ou mais símbolos idênticos em sequência.

```java
@Override
public boolean check(Board board) {
    int size = board.getSize();
    for (int i = 0; i < size; i++) {
        if (!checkLine(board, i, true)) return false;
        if (!checkLine(board, i, false)) return false;
    }
    return true;
}
```

O método `check()` é o ponto de entrada da regra. Ele obtém o tamanho do tabuleiro e inicia um loop de `i = 0` até `size - 1`. Para cada valor de `i`, chama `checkLine` duas vezes: uma com `isRow = true` (verificando a linha de índice `i`) e outra com `isRow = false` (verificando a coluna de índice `i`). Se qualquer chamada retornar `false`, o método retorna `false` imediatamente sem verificar as linhas e colunas restantes. Se todas as linhas e colunas passarem, retorna `true`.

```java
private boolean checkLine(Board board, int lineIndex, boolean isRow) {
    int size = board.getSize();
    Symbol previous = null;
    int consecutive = 1;

    for (int j = 0; j < size; j++) {
        int row = isRow ? lineIndex : j;
        int col = isRow ? j : lineIndex;
        Symbol current = board.getCell(row, col).getSymbol();

        if (current == null) {
            previous = null;
            consecutive = 1;
            continue;
        }

        if (current == previous) {
            consecutive++;
            if (consecutive > MAX_CONSECUTIVE) return false;
        } else {
            previous = current;
            consecutive = 1;
        }
    }
    return true;
}
```

`checkLine` percorre uma linha ou coluna posição por posição, mantendo dois controles: `previous` (o símbolo visto na posição anterior) e `consecutive` (quantas vezes esse símbolo apareceu seguido). Antes do loop, `previous = null` (nenhum símbolo anterior ainda) e `consecutive = 1` (valor inicial irrelevante, pois o primeiro símbolo sempre redefine o contador para 1 na branch `else`).

Dentro do loop, `row` e `col` são calculados de forma flexível: se `isRow` é `true`, `row` fica fixo em `lineIndex` e `col` avança com `j`, percorrendo uma linha da esquerda para a direita. Se `isRow` é `false`, `col` fica fixo em `lineIndex` e `row` avança com `j`, percorrendo uma coluna de cima para baixo. Esse mecanismo permite reutilizar o mesmo algoritmo para linhas e colunas.

O símbolo na posição atual é obtido com `board.getCell(row, col).getSymbol()`. Se o resultado é `null`, a célula está vazia. Nesse caso, o código executa `previous = null; consecutive = 1; continue`, que reseta ambos os controles e pula para a próxima posição. Esse reset é a decisão mais importante da implementação: ao encontrar uma célula vazia, o contador é zerado porque não sabemos qual símbolo será colocado ali. Imaginar que a sequência continua através de uma célula vazia geraria falsos positivos — o código rejeitaria estados parciais perfeitamente válidos.

Se o símbolo atual não é `null`, o código compara com `previous`. Se são iguais (`current == previous`), incrementa `consecutive`. Se `consecutive` ultrapassa `MAX_CONSECUTIVE` (que é `2`), retorna `false` imediatamente — violação detectada. Se os símbolos são diferentes, a sequência foi quebrada: `previous` recebe o símbolo atual e `consecutive` volta para `1`.

---

### 6.2 BalanceRule — Regra do Equilíbrio

Esta regra verifica que cada linha e coluna possui exatamente `tamanho/2` Sóis e `tamanho/2` Luas.

```java
@Override
public boolean check(Board board) {
    int size = board.getSize();
    int half = size / 2;
    for (int i = 0; i < size; i++) {
        if (!checkLine(board, i, true, half)) return false;
        if (!checkLine(board, i, false, half)) return false;
    }
    return true;
}
```

O método principal calcula `half = size / 2` (para 6×6, `half = 3`; para 8×8, `half = 4`) e passa esse valor para `checkLine`. O loop percorre todas as linhas e colunas, retornando `false` imediatamente ao encontrar qualquer violação.

```java
private boolean checkLine(Board board, int lineIndex, boolean isRow, int half) {
    int size = board.getSize();
    int sunCount = 0, moonCount = 0, remaining = 0;

    for (int j = 0; j < size; j++) {
        int row = isRow ? lineIndex : j;
        int col = isRow ? j : lineIndex;
        Symbol symbol = board.getCell(row, col).getSymbol();

        if      (symbol == Symbol.SUN)  sunCount++;
        else if (symbol == Symbol.MOON) moonCount++;
        else                            remaining++;
    }

    if (sunCount > half || moonCount > half) return false;
    if (sunCount  + remaining < half)        return false;
    if (moonCount + remaining < half)        return false;
    return true;
}
```

`checkLine` percorre a linha ou coluna contando três quantidades separadas: `sunCount` conta quantos Sóis já foram atribuídos, `moonCount` conta quantas Luas, e `remaining` conta quantas células ainda estão vazias (`null`). A estrutura `if/else if/else` garante que cada célula contribui para exatamente um dos três contadores.

Após o loop, o código aplica três verificações sequenciais:

A **primeira verificação** (`sunCount > half || moonCount > half`) detecta quando um símbolo já foi colocado mais vezes do que o permitido. Se `sunCount` já passou de `half`, não há como equilibrar — mesmo que todas as células restantes fossem Lua, os Sóis em excesso não podem ser removidos. Retorna `false` imediatamente.

A **segunda verificação** (`sunCount + remaining < half`) detecta quando é impossível atingir o mínimo de Sóis, mesmo no melhor cenário. A lógica é: se somarmos todos os Sóis já presentes com todos os espaços vagos (que poderiam ser preenchidos com Sol), e mesmo assim não chegamos ao `half`, então é matematicamente impossível ter `half` Sóis nessa linha — retorna `false`.

A **terceira verificação** (`moonCount + remaining < half`) aplica o mesmo raciocínio para Luas.

A presença de `remaining` nessas verificações é o que transforma essa checagem de "verificação simples" para **forward-checking**: o código não apenas confirma o que já aconteceu, mas detecta antecipadamente o que é impossível de acontecer. Uma versão sem `remaining` — apenas `sunCount <= half && moonCount <= half` — deixaria passar estados que nunca poderiam levar a uma solução balanceada, desperdiçando trabalho de busca.

---

### 6.3 EqualRule — Regra da Igualdade

```java
@Override
public boolean check(Board board) {
    for (Constraint constraint : board.getConstraints()) {
        if (constraint.getType() != ConstraintType.EQUAL) continue;

        Symbol first  = board.getCellAt(constraint.getFirstIndex()).getSymbol();
        Symbol second = board.getCellAt(constraint.getSecondIndex()).getSymbol();

        if (first != null && second != null && first != second) {
            return false;
        }
    }
    return true;
}
```

O método percorre todas as constraints do tabuleiro com um `for-each`. Para cada constraint, a primeira coisa que faz é verificar se o tipo é `EQUAL`. Se não for, executa `continue` — pula para a próxima constraint sem fazer mais nada. Esse filtro garante que a `EqualRule` só processa arestas de igualdade, ignorando as de oposição (que serão tratadas pela `OppositionRule`).

Para cada constraint de igualdade, o código busca os símbolos das duas células envolvidas. `constraint.getFirstIndex()` retorna o índice linear da primeira célula, e `board.getCellAt(...)` retorna o objeto `Cell` naquele índice. `.getSymbol()` extrai o símbolo atual — que pode ser `SUN`, `MOON` ou `null` se a célula está vazia.

A condição de falha é `first != null && second != null && first != second`. Essa expressão precisa de três partes para ser correta. A primeira parte `first != null` garante que a primeira célula já foi preenchida. A segunda `second != null` garante que a segunda também foi. A terceira `first != second` detecta que os dois símbolos são diferentes quando deveriam ser iguais. Se qualquer uma das duas primeiras condições for falsa (ou seja, uma célula ainda está vazia), o resultado inteiro da expressão é `false` por curto-circuito do `&&`, e a violação não é reportada — correto, pois ainda não sabemos o que será colocado na célula vazia. Se ambas estão preenchidas e são diferentes, retorna `false`.

---

### 6.4 OppositionRule — Regra da Oposição

```java
@Override
public boolean check(Board board) {
    for (Constraint constraint : board.getConstraints()) {
        if (constraint.getType() != ConstraintType.OPPOSITE) continue;

        Symbol first  = board.getCellAt(constraint.getFirstIndex()).getSymbol();
        Symbol second = board.getCellAt(constraint.getSecondIndex()).getSymbol();

        if (first != null && second != null && first == second) {
            return false;
        }
    }
    return true;
}
```

A estrutura é idêntica à `EqualRule`, com duas diferenças: o filtro agora verifica `ConstraintType.OPPOSITE` em vez de `EQUAL`, e a condição de violação é `first == second` em vez de `first != second`. Ou seja, se as duas células estão preenchidas e têm o **mesmo** símbolo quando deveriam ser opostas, a regra é violada.

---

## 7. Framework CSP Genérico

O framework CSP define quatro interfaces que encapsulam os conceitos fundamentais de qualquer problema de satisfação de restrições. Nenhuma delas menciona Tango, Sol, Lua ou qualquer regra específica.

### 7.1 State — o Estado do Problema

```java
public interface State<S> {
    S       clone();
    void    assign(int variable, int value);
    void    unassign(int variable);
    boolean isComplete();
}
```

`State<S>` é uma interface genérica parametrizada por `S`, que representa o tipo do próprio estado. O `<S>` existe para que `clone()` possa retornar o tipo concreto correto em vez de retornar `Object`.

O método `assign(int variable, int value)` recebe dois inteiros: o identificador da variável a ser atribuída (no Tango, o índice linear de uma célula) e o valor a atribuir (no Tango, `0` para Sol ou `1` para Lua). O método não retorna nada — ele apenas muda o estado interno do objeto.

O método `unassign(int variable)` desfaz a atribuição da variável identificada por `variable`, retornando-a ao estado "não atribuído" — no Tango, isso significa setar o símbolo da célula como `null`.

O método `isComplete()` retorna `true` quando todas as variáveis foram atribuídas — no Tango, quando não existe nenhuma célula vazia.

O método `clone()` cria e retorna uma cópia profunda e independente do estado atual. É chamado pelo engine quando uma solução é encontrada, para preservar o estado final antes que as operações de desfazimento o modifiquem.

### 7.2 ConstraintChecker — o Verificador

```java
public interface ConstraintChecker<S> {
    boolean isValid(S state);
}
```

Recebe um estado e retorna `true` se ele é válido segundo as regras do problema. No Backtracking, é chamado após cada atribuição individual (estado parcial). Na Força Bruta, é chamado somente quando o estado está completo.

### 7.3 VariableSelector — Qual Variável Atribuir a Seguir

```java
public interface VariableSelector<S> {
    OptionalInt selectVariable(S state);
}
```

Recebe o estado atual e retorna o identificador da próxima variável a ser atribuída. O retorno é `OptionalInt` — um tipo Java que pode conter um inteiro ou estar vazio — para indicar que não há mais variáveis livres (todas foram atribuídas). Nesse caso, o engine sabe que o estado está completo.

### 7.4 ValueIterator — Quais Valores Tentar

```java
public interface ValueIterator<S> {
    List<Integer> getValues(S state, int variable);
}
```

Para uma variável escolhida, retorna a lista de valores candidatos a tentar. No Tango, sempre `[0, 1]`. Em outros problemas, essa interface poderia filtrar valores impossíveis antes de tentar, reduzindo ainda mais o espaço de busca.

### 7.5 SolveResult — o Resultado Imutável

```java
public final class SolveResult<S> {
    private final S    solution;
    private final long nodesVisited;
    private final long backtracks;
    private final long attempts;
}
```

Todos os campos são `final`, tornando o objeto completamente imutável após a criação. `solution` guarda o estado final encontrado, ou `null` se o problema não tem solução. `nodesVisited` conta as entradas na função recursiva. `backtracks` conta as podas (no Backtracking) ou retrocessos simples (na Força Bruta). `attempts` conta as chamadas ao `ConstraintChecker`.

---

## 8. Algoritmo de Força Bruta

### 8.1 Conceito

A Força Bruta é a abordagem mais direta: gera sistematicamente todas as combinações possíveis de símbolos para as células vazias e, para cada tabuleiro completo gerado, verifica se é uma solução válida. Não há nenhuma tentativa de evitar combinações ruins durante a geração — o algoritmo não olha para as regras até que o tabuleiro esteja completamente preenchido.

### 8.2 O Método `generate()` — Linha a Linha

```java
private S generate(S state) {
    nodesVisited++;

    if (state.isComplete()) {
        attempts++;
        return checker.isValid(state) ? state : null;
    }

    OptionalInt variableOpt = selector.selectVariable(state);
    if (variableOpt.isEmpty()) return null;

    int variable = variableOpt.getAsInt();

    for (int value : iterator.getValues(state, variable)) {
        state.assign(variable, value);
        S candidate = generate(state);
        if (candidate != null) return candidate;
        backtracks++;
        state.unassign(variable);
    }

    return null;
}
```

A primeira linha do método, `nodesVisited++`, incrementa o contador de nós visitados. Isso acontece absolutamente toda vez que `generate()` é chamado — independente do que aconteça depois. Cada chamada recursiva representa um nó na árvore de busca, e contar todas elas dá a dimensão real do trabalho realizado.

Em seguida, `if (state.isComplete())` verifica se o tabuleiro está totalmente preenchido. Se estiver, é o momento de avaliar: `attempts++` registra mais uma verificação de tabuleiro completo, e `checker.isValid(state)` aplica as 5 regras sobre o tabuleiro completo. Se for válido, retorna o próprio `state` — a solução foi encontrada. Se não for válido, retorna `null` — esse tabuleiro completo não é solução, precisa tentar outra combinação.

Se o tabuleiro **não** está completo, `selector.selectVariable(state)` escolhe a próxima célula vazia. O resultado é um `OptionalInt` — se estiver vazio (o que na prática não acontece aqui porque `isComplete()` já teria retornado `true`), retorna `null`.

O `for (int value : iterator.getValues(state, variable))` itera sobre os valores candidatos, que são sempre `[0, 1]` (Sol e Lua). Para cada valor: `state.assign(variable, value)` atribui o símbolo à célula; `generate(state)` é chamado recursivamente com o tabuleiro agora uma célula mais preenchido; se o resultado não é `null`, uma solução foi encontrada em algum ponto abaixo — retorna imediatamente com `return candidate` (Early Exit); se o resultado é `null`, esse caminho não levou a solução — `backtracks++` registra o retrocesso, e `state.unassign(variable)` desfaz a atribuição, voltando a célula ao estado vazio para tentar o próximo valor.

Se ambos os valores foram tentados e nenhum produziu solução, o loop termina e o método retorna `null` — propagando para cima a informação de que esse caminho não tem solução.

**O ponto mais importante:** `checker.isValid(state)` aparece **exclusivamente dentro do `if (state.isComplete())`**. Em nenhum outro lugar do método a validação é chamada. Isso é o que define a Força Bruta: ela gera combinações sem verificar nada durante a geração, e só avalia quando o tabuleiro está completo.

### 8.3 Complexidade e Limites Práticos

Com `k` células vazias e 2 valores possíveis por célula, o número máximo de tabuleiros completos avaliados é 2^k. Para k=30 (6×6 easy), isso é ~1 bilhão — levou 9,82 segundos. Para k=49 (8×8), seria ~562 trilhões — à mesma taxa, levaria aproximadamente 5 anos. Por isso o código desabilita a Força Bruta para grades maiores que 6×6.

---

## 9. Algoritmo de Backtracking

### 9.1 Conceito

O Backtracking adiciona um mecanismo crucial que a Força Bruta não tem: valida o estado **após cada atribuição individual**, mesmo que o tabuleiro ainda esteja incompleto. Se detectar violação, descarta imediatamente toda a subárvore de combinações que derivariam daquele estado — sem gerá-las. Isso é chamado de poda.

### 9.2 O Método `backtrack()` — Linha a Linha

```java
private void backtrack(S state) {
    nodesVisited++;

    if (state.isComplete()) {
        if (firstSolution == null) firstSolution = state.clone();
        return;
    }

    OptionalInt variableOpt = selector.selectVariable(state);
    if (variableOpt.isEmpty()) return;

    int variable = variableOpt.getAsInt();

    for (int value : iterator.getValues(state, variable)) {
        state.assign(variable, value);
        attempts++;

        if (checker.isValid(state)) {
            backtrack(state);
            if (firstSolution != null) return;
        } else {
            backtracks++;
        }

        state.unassign(variable);
    }
}
```

`nodesVisited++` incrementa o contador a cada entrada na função, assim como na Força Bruta.

`if (state.isComplete())` verifica se o tabuleiro está completo. Se estiver, a solução foi encontrada. O código salva `state.clone()` em `firstSolution` — e não o próprio `state` — porque `state` é o objeto de trabalho mutável. As chamadas recursivas que ainda precisam retornar vão continuar desfazendo atribuições nele. Se salvasse a referência direta, esses desfazimentos corromperiam a solução salva. O clone cria uma cópia independente naquele exato instante. Depois do clone, `return` encerra a função — não há mais nada a fazer neste nível.

`selector.selectVariable(state)` escolhe a próxima célula vazia. O mesmo mecanismo da Força Bruta.

O `for` itera sobre os dois valores candidatos. A diferença crucial em relação à Força Bruta começa aqui:

`state.assign(variable, value)` atribui o símbolo. Logo depois, `attempts++` registra que uma verificação será feita.

`if (checker.isValid(state))` chama o verificador **agora**, com o tabuleiro ainda incompleto — com células vazias em todas as posições ainda não processadas. Se o estado parcial já viola alguma regra (por exemplo, a célula recém-preenchida criou três Sóis consecutivos na linha), o `if` entra no `else`:

`backtracks++` registra a poda — aqui é contado cada atribuição que foi rejeitada por violar uma regra.

Se `checker.isValid(state)` retorna `true`, o estado parcial é aceitável e vale a pena continuar: `backtrack(state)` é chamado recursivamente. Se durante essa recursão a solução foi encontrada, `firstSolution` não é mais `null`, e `if (firstSolution != null) return` encerra imediatamente este nível sem tentar o segundo valor — propagando o sucesso para cima.

`state.unassign(variable)` é a última linha do loop e desfaz a atribuição. Ela é executada em dois casos: quando a verificação falhou (depois de `backtracks++`) e quando a recursão retornou sem solução (depois que `firstSolution` ainda é `null`). Isso é o backtracking: desfazer o passo e tentar a alternativa.

### 9.3 Por que a Poda é tão Efetiva

Cada poda elimina toda a subárvore abaixo do nó podado. Se uma violação é detectada com, digamos, 10 células ainda por preencher, a poda elimina 2^10 = 1024 tabuleiros completos de uma vez. Se a violação é detectada com 20 células por preencher, elimina 2^20 ≈ 1 milhão de tabuleiros. Quanto mais cedo as regras detectam violações — e as 5 regras do Tango são bastante restritivas mesmo em estados parciais — maiores são as subárvores eliminadas.

---

## 10. Adaptador e Seleção de Variável

### 10.1 TangoBoardAdapter — a Ponte Entre os Mundos

```java
public final class TangoBoardAdapter implements State<TangoBoardAdapter> {

    private final Board board;

    public TangoBoardAdapter clone() {
        return new TangoBoardAdapter(BoardFactory.deepCopy(board));
    }

    public void assign(int variable, int value) {
        board.getCellAt(variable).setSymbol(Symbol.values()[value]);
    }

    public void unassign(int variable) {
        board.getCellAt(variable).setSymbol(null);
    }

    public boolean isComplete() {
        for (int i = 0; i < board.cellCount(); i++)
            if (board.getCellAt(i).isEmpty()) return false;
        return true;
    }
}
```

O `TangoBoardAdapter` implementa `State<TangoBoardAdapter>`, o que significa que o engine pode manipulá-lo usando apenas os quatro métodos da interface, sem precisar conhecer o `Board` internamente.

O campo `board` guarda o tabuleiro real. O adaptador é um invólucro fino: ele traduz as operações genéricas do engine para operações concretas no `Board`.

O método `clone()` cria uma nova instância de `TangoBoardAdapter` envolvendo uma cópia profunda do tabuleiro, produzida por `BoardFactory.deepCopy(board)`. Essa cópia é independente, de modo que modificações na cópia não afetam o original.

O método `assign(int variable, int value)` traduz a operação genérica "atribua o valor `value` à variável `variable`" para a operação concreta "coloque o símbolo `Symbol.values()[value]` na célula de índice `variable`". A expressão `Symbol.values()[value]` acessa o array interno do enum `Symbol` — `Symbol.values()` retorna `[SUN, MOON]`, então índice `0` dá `SUN` e índice `1` dá `MOON`. A célula é acessada com `board.getCellAt(variable)` (acesso direto por índice linear) e o símbolo é atribuído com `setSymbol()`.

O método `unassign(int variable)` faz o inverso: chama `setSymbol(null)` na célula do índice `variable`, tornando-a vazia novamente.

O método `isComplete()` percorre todas as células de `0` até `board.cellCount() - 1`. Para cada célula, chama `isEmpty()`. Se qualquer célula estiver vazia, retorna `false` imediatamente. Se o loop terminar sem encontrar nenhuma vazia, retorna `true`. Células fixas nunca são vazias (têm símbolo desde a criação pelo parser), então elas não influenciam o resultado — apenas as células livres determinam a completude.

### 10.2 TangoVariableSelector — Qual Célula Preencher a Seguir

```java
public OptionalInt selectVariable(TangoBoardAdapter adapter) {
    Board board = adapter.getBoard();
    for (int i = 0; i < board.cellCount(); i++) {
        if (board.getCellAt(i).isEmpty()) return OptionalInt.of(i);
    }
    return OptionalInt.empty();
}
```

O método extrai o `Board` do adaptador e percorre as células em ordem linear crescente, de `0` até `cellCount() - 1`. Para cada célula, verifica se está vazia. A primeira célula vazia encontrada tem seu índice retornado imediatamente com `OptionalInt.of(i)`. Se o loop terminar sem encontrar nenhuma célula vazia — o que só acontece quando o tabuleiro está completo — retorna `OptionalInt.empty()`.

Células fixas têm `isEmpty() == false` desde o início (têm símbolo definido pelo parser), então são automaticamente puladas. O engine nunca tenta atribuir ou desatribuir uma célula fixa.

A estratégia de selecionar sempre a primeira célula vazia em ordem linear é a mais simples possível. Ela cria uma árvore de busca previsível e funciona bem para os tamanhos de tabuleiro do projeto. Uma heurística mais sofisticada — como escolher a célula com mais constraints associadas, que tenderia a gerar podas mais agressivas — poderia melhorar o desempenho, mas não é necessária para os tabuleiros disponíveis.

### 10.3 TangoValueIterator — Quais Valores Tentar

```java
private static final List<Integer> SYMBOL_VALUES = List.of(0, 1);

public List<Integer> getValues(TangoBoardAdapter adapter, int variable) {
    return SYMBOL_VALUES;
}
```

O método retorna sempre `[0, 1]` independente de qual célula está sendo considerada e do estado atual do tabuleiro. A lista `SYMBOL_VALUES` é uma constante estática criada uma única vez quando a classe é carregada — não é recriada a cada chamada, evitando alocação de memória desnecessária num método que pode ser chamado centenas de milhões de vezes.

O valor `0` corresponde a `SUN` e `1` corresponde a `MOON`, pela convenção estabelecida no `assign()` do adaptador. O engine sempre tenta `SUN` primeiro. Se `SUN` violar uma regra ou não levar a solução, tenta `MOON`. Essa ordem é determinista — executar o mesmo programa com o mesmo tabuleiro sempre produz o mesmo resultado.

---

## 11. Métricas e Cálculos

### 11.1 Nós Visitados

Incrementado no início de cada chamada à função recursiva — tanto `backtrack()` quanto `generate()`. Representa o total de estados que o algoritmo entrou para examinar, incluindo o estado inicial (raiz), todos os estados intermediários e os estados completos (folhas da árvore). É a medida mais direta do trabalho bruto realizado.

Para a Força Bruta, a relação entre nós e verificações é sempre próxima de 2:1 porque numa árvore binária completa, o número de nós internos é aproximadamente igual ao número de folhas, fazendo o total de nós ≈ 2 × número de folhas. Como as verificações só acontecem nas folhas (tabuleiros completos), isso explica a relação observada nos dados reais: 514.505.331 nós ≈ 2 × 257.252.659 verificações para o 6×6-easy.

### 11.2 Verificações de Restrição

Conta o número de chamadas ao `checker.isValid()`. O significado é diferente para cada algoritmo: no Backtracking, é chamado uma vez para cada `assign()`, portanto conta quantas vezes o estado parcial foi verificado. Na Força Bruta, é chamado apenas dentro de `isComplete()`, portanto conta quantos tabuleiros completos foram avaliados.

### 11.3 Podas no Backtracking

```java
long prunings = isBT ? solveResult.getBacktracks() : 0L;
```

Para o Backtracking, `backtracks` conta atribuições rejeitadas por violar uma regra. Para a Força Bruta, esse campo contém retrocessos simples (células desfazer sem validação), que são diferentes conceitualmente de podas — por isso são zerados na saída, com `0L`.

### 11.4 Percentual do Espaço Explorado

```java
public double percentExplored() {
    long evaluated = algorithm.equals("bf") ? constraintChecks : nodesVisited;
    if (emptyCells >= 62) return evaluated * 100.0 / (double)(1L << 62);
    return evaluated * 100.0 / (double)(1L << emptyCells);
}
```

O método seleciona o numerador: para Força Bruta usa `constraintChecks` (tabuleiros completos avaliados), para Backtracking usa `nodesVisited`. O denominador é `2^emptyCells` — o tamanho teórico total do espaço de busca. A divisão dá a fração do espaço efetivamente coberta.

A condição `if (emptyCells >= 62)` existe para evitar overflow: em Java, `1L << 63` seria negativo (overflow do tipo `long`, que tem 64 bits com sinal). Para evitar isso, quando há 62 ou mais células vazias, usa `1L << 62` como denominador — uma aproximação conservadora que ainda expressa a ideia de "fração minúscula de um espaço imenso".

O cálculo `100.0 / (double)(1L << emptyCells)` converte o denominador para `double` antes da divisão, necessário porque `1L << 30` cabe num `long` mas a divisão com `long` seria inteira, truncando o resultado.

### 11.5 Eficiência de Poda

```java
public double pruningRate() {
    long denom = nodesVisited + backtracks;
    return denom == 0 ? 0 : backtracks * 100.0 / denom;
}
```

Calcula: de todas as "decisões" tomadas pelo algoritmo (`nodesVisited` entradas na função + `backtracks` podas), qual fração foi poda. Um valor alto significa que o algoritmo está rejeitando caminhos cedo com frequência.

Para o 16×16: `206.658 / (206.771 + 206.658) = 206.658 / 413.429 ≈ 99,94%` — quase toda tentativa resulta em poda. Para o 4×4: `9 / (15 + 9) = 37,5%` — menos de metade. Isso mostra que as regras do Tango são proporcionalmente mais restritivas em tabuleiros maiores, pois mais células fixas e constraints interagem.

### 11.6 Nós por Segundo

```java
public double nodesPerSecond() {
    return timeNs == 0 ? 0 : nodesVisited / (timeNs / 1_000_000_000.0);
}
```

`timeNs` é o tempo em nanosegundos. Dividir por `1_000_000_000.0` converte para segundos. A divisão de `nodesVisited` por esse valor dá quantos nós foram processados por segundo. O `timeNs == 0` evita divisão por zero se a execução foi tão rápida que o cronômetro não registrou tempo mensurável.

---

## 12. Resultados de Execução

### 12.1 Desempenho por Execução

| Board | Algoritmo | Tempo | Nós Visitados | Verif. Restrição | Podas | % Espaço |
|---|---|---|---|---|---|---|
| 4×4 | Backtracking | 2,19 ms | 15 | 23 | 9 | 0,1831% |
| 4×4 | Força Bruta | 4,63 ms | 7.772 | 3.884 | 0 | 47,4121% |
| 6×6-easy | Backtracking | 138 µs | 32 | 48 | 17 | ~3×10⁻⁶% |
| 6×6-easy | Força Bruta | 9,82 s | 514.505.331 | 257.252.659 | 0 | 23,9585% |
| 6×6-medium | Backtracking | 195 µs | 37 | 58 | 22 | ~3×10⁻⁶% |
| 6×6-medium | Força Bruta | 16,75 s | 785.176.951 | 392.588.469 | 0 | 36,5627% |
| 6×6-hard | Backtracking | 105 µs | 47 | 78 | 32 | ~1×10⁻⁶% |
| 6×6-hard | Força Bruta | 121,06 s | 5.686.273.731 | 2.843.136.859 | 0 | 66,1969% |
| 8×8 | Backtracking | 382 µs | 183 | 338 | 156 | ~3×10⁻¹¹% |
| 8×8 | Força Bruta | inviável | — | — | — | 2^49 |
| 16×16 | Backtracking | 335,81 ms | 206.771 | 413.428 | 206.658 | ~4×10⁻¹²% |
| 16×16 | Força Bruta | inviável | — | — | — | 2^224 |

### 12.2 Comparativo BT vs BF

| Board | Speedup BT | Nós BT | Nós BF | Redução de Nós | Ef. Poda BT |
|---|---|---|---|---|---|
| 4×4 | 2× | 15 | 7.772 | 99,807% | 37,50% |
| 6×6-easy | 71.096× | 32 | 514.505.331 | ~100% | 34,69% |
| 6×6-medium | 85.498× | 37 | 785.176.951 | ~100% | 37,29% |
| 6×6-hard | 1.152.909× | 47 | 5.686.273.731 | ~100% | 40,51% |

### 12.3 Verificações de Restrição

| Board | Verif. BT | Verif. BF | Razão BF/BT | Podas BT |
|---|---|---|---|---|
| 4×4 | 23 | 3.884 | 169× | 9 |
| 6×6-easy | 48 | 257.252.659 | 5.359.430× | 17 |
| 6×6-medium | 58 | 392.588.469 | 6.768.767× | 22 |
| 6×6-hard | 78 | 2.843.136.859 | 36.450.473× | 32 |

---

## 13. Análise Comparativa

### 13.1 O que os Números Revelam

A comparação mais impactante é a razão de verificações de restrição. Para o 6×6-hard, o Backtracking precisou de 78 verificações enquanto a Força Bruta precisou de 2.843.136.859 — 36 milhões de vezes mais. Cada verificação do BT acontece sobre um estado parcial logo após uma atribuição. Cada verificação do BF acontece sobre um tabuleiro de 36 células completo. Mas mesmo considerando que a verificação do BF é ligeiramente mais cara (tabuleiro mais preenchido), a diferença de 36 milhões nunca é compensada por esse fator. A razão real é estrutural: as 32 podas do BT eliminaram subárvores inteiras que a BF teve que percorrer célula por célula.

### 13.2 Por que a Dificuldade do Tabuleiro Afeta os Algoritmos Diferentemente

O 6×6-hard é muito mais difícil para a Força Bruta do que para o Backtracking, mas essa dificuldade não é a mesma coisa que dificuldade humana. A dificuldade para a BF é determinada pelo tamanho do espaço de busca (número de células vazias) e pela posição da solução nesse espaço (quão cedo no espaço o Early Exit encontra a resposta). O 6×6-hard tem 32 células vazias versus 30 do 6×6-easy, e a solução estava em 66% do espaço versus 24% — ambos os fatores combinados fizeram a BF levar ~12 vezes mais tempo no hard do que no easy.

Para o Backtracking, a dificuldade é determinada pela efetividade das podas, que depende das constraints e dicas fixas disponíveis. Um tabuleiro com menos dicas pode ser mais fácil para o BT se suas constraints estiverem bem posicionadas para gerar podas precoces.

### 13.3 Escalabilidade e Viabilidade

| Tamanho | BF viável? | Espaço de busca | Tempo estimado BF |
|---|---|---|---|
| 4×4 (~13 vazios) | Sim | 2^13 ≈ 8.000 | milissegundos |
| 6×6 (~30-32 vazios) | Marginalmente | 2^30-32 ≈ 1-4 bilhões | segundos a minutos |
| 8×8 (~49 vazios) | Não | 2^49 ≈ 562 trilhões | anos |
| 16×16 (~224 vazios) | Impossível | 2^224 | mais que a idade do universo |

O Backtracking resolve o 16×16 em 335ms com taxa de poda de 99,94%. Isso acontece porque as 5 regras do Tango são altamente restritivas: cada atribuição tem alta probabilidade de violar ao menos uma regra, gerando podas que eliminam subárvores exponencialmente grandes. Quanto maior o tabuleiro, maior cada subárvore eliminada — e paradoxalmente mais eficiente fica o BT em termos relativos. O BF não se beneficia disso porque não poda: cada atribuição errada só é descoberta no final, depois de preencher todas as células restantes.
