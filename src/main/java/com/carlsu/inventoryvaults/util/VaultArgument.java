package com.carlsu.inventoryvaults.util;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

public class VaultArgument implements ArgumentType<Vault>{
    // private static final Collection<String> EXAMPLES = Stream.of(Level.OVERWORLD, Level.NETHER).map((p_88814_) -> {
    //   return p_88814_.location().toString();
    // }).collect(Collectors.toList());

    @Override
    public Vault parse(StringReader reader) throws CommandSyntaxException {
        throw new UnsupportedOperationException("Unimplemented method 'parse'");
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return Collections.emptyList();
    }
    
}
