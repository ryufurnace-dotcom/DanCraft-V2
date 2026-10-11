# Dancraft — Minecraft 1.20.1 (Forge 47.x)

Mod RPG baseado em DanMachi. Esta é a **Fase 1 (Fundação do RPG)** do plano.

## Como rodar
1. Instale o **JDK 17**.
2. Este projeto não inclui o *Gradle Wrapper*. Baixe o **Forge MDK 1.20.1** (files.minecraftforge.net), copie
   `gradlew`, `gradlew.bat` e a pasta `gradle/` dele para esta pasta (ou rode `gradle wrapper` se tiver o Gradle).
3. Abra a pasta no IntelliJ IDEA (importar como projeto Gradle) e rode `./gradlew genIntellijRuns` e depois `runClient`.
   Para gerar o .jar: `./gradlew build` (saída em `build/libs`).

> O código foi escrito sem acesso à internet, então **ainda não foi compilado contra o Forge**. A sintaxe foi checada,
> mas pode haver pequenos ajustes de API ao compilar pela primeira vez.

## Compilar no GitHub
Suba o projeto para um repositório: a aba **Actions** compila sozinha (`.github/workflows/build.yml`) e o .jar fica em *Artifacts* ao fim do job. Se houver erro de compilação, ele aparece no log do job.

## O que já existe
- **Tela inicial** com a arte (`assets/dancraft/textures/gui/title_background.png`, texto japonês removido).
- **XP e nível 1–100** (curva em `PlayerRpg.xpForLevel`), 3 pontos de atributo por nível.
- **Atributos** Força, Agilidade, Resistência, Destreza e Magia (Magia fica para a fase de magia) ligados aos atributos do Minecraft.
- **Excelia** por feitos (primeiro chefe, biomas novos, conquistas) e **Falna nível 1–10**, com atualização de status.
- **Fontes de XP**: minerar, cortar, colher, fabricar, fundir, pescar, matar monstros, explorar biomas, conquistas.
  Cada fonte tem **fadiga** (`XpSource`): repetir a mesma ação rende cada vez menos e recupera com o tempo.
- **Painel de status** (tecla **K**) e **HUD** de nível/XP no canto superior esquerdo.
- **Pergaminho da Falna** (provisório, receita: papel + lingote de ouro + lápis-lazúli) para receber a Falna.
- **Comandos de teste** (OP): `/dancraft xp add <n>`, `excelia add <n>`, `falna grant`, `falna set <0-10>`, `info`, `reset`.

## Próximas fases
Combate e Mastery → Magia, raças e traits (spins) → Dungeon e criaturas → Campanha e Orario → Endgame.

## Ajustes rápidos de balanceamento
- Ritmo global de XP: `RpgManager.XP_RATE`
- Curva de nível: `PlayerRpg.xpForLevel`
- Excelia por nível da Falna: `PlayerRpg.FALNA_EXCELIA`
- Bônus por atributo: constantes em `RpgAttribute`
- XP por fonte e fadiga: `BlockXp`, `RpgEvents`, `XpSource`
