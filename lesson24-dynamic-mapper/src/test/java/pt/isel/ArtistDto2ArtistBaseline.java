package pt.isel;

import static pt.isel.DynamicMapperKt.loadDynamicMapper;

public class ArtistDto2ArtistBaseline implements Mapper<ArtistDto, Artist> {
    @Override
    public Artist mapFrom(ArtistDto src) {
        return new Artist(
                src.getKind(),
                src.getName(),
                loadDynamicMapper(StateDto.class, Country.class).mapFrom(src.getCountry()),
                loadDynamicMapper(SongDto.class, Track.class).mapFromList(src.getSongs())
        );
    }
}
