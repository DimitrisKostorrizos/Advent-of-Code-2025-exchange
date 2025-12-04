use strict;
use warnings;
use feature 'say';


my $total_joltage = 0;

my $fh;
if (@ARGV) {
    open $fh, '<', $ARGV[0] or die "Can't open file '$ARGV[0]': $!";
} else {
    $fh = *DATA;
}

while (my $row = <$fh>) {
    chomp $row;                     # drop newline
    next unless $row;               # skip empty lines

    my @ROW = split //, $row;

    my @answer = (0) x 12;
    my $answer_length = scalar @answer;
    
    my $pos = 0;
    while ($pos <= scalar(@ROW) - $answer_length) {
        for (my $i=0; $i<$answer_length; $i++) {
            if ($ROW[$pos+$i] > $answer[$i]) {
                $answer[$i] = $ROW[$pos+$i];
                $answer[$_] = 0 for $i+1 .. ($answer_length-1);
            }
        }
        for my $n (1 .. ($answer_length-1)) {
            $answer[$n] = $ROW[$pos+$n] if $ROW[$pos+$n] > $answer[$n];
        }
        
        $pos++;
    }

    # say join '', @answer;
    $total_joltage += join '', @answer;
}
close $fh;

say "total output joltage is: $total_joltage";


__DATA__

987654321111111
811111111111119
234234234234278
818181911112111