package me.shedaniel.rei.api;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class TextTest {
    public static void main(String[] args) {
        MutableText text = new LiteralText("adaw").append("dawdwdaw").formatted(Formatting.RED);
        System.out.println(text.getString());
        System.out.println(text.getString());
    }
}
