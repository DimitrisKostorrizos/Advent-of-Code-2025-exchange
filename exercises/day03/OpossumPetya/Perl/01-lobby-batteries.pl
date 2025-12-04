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

    my $needed_numbers= 2;
    my @answer = (0, 0);
    
    my $pos = 0;
    while ($pos < scalar(@ROW)-1) {
        if ($ROW[$pos] > $answer[0]) {
            $answer[0] = $ROW[$pos];
            $answer[1] = 0;
        }
        $answer[1] = $ROW[$pos+1] if $ROW[$pos+1] > $answer[1];
        $pos++;
    }

    # say $row;
    # say $answer[0].$answer[1];
    $total_joltage += "$answer[0]$answer[1]";
}
close $fh;

say "total output joltage is: $total_joltage";


__DATA__

987654321111111
811111111111119
234234234234278
818181911112111